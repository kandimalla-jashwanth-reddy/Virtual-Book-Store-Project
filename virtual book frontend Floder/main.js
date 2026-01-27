// ================= API CONFIG =================
const API_BASE_URL = "http://localhost:9090/api";

// ================= DOM ELEMENTS =================
const logoutBtn = document.getElementById("logout");
const userGreeting = document.getElementById("user-greeting");
const cartCount = document.querySelector(".cart-count");

// ================= APP INIT =================
document.addEventListener("DOMContentLoaded", () => {
    renderUserMenu();
    setupEventListeners();

    if (document.getElementById("booksContainer")) {
        loadBooks();
    }

    if (window.location.pathname.includes("index.html") || window.location.pathname === "/") {
        loadFeaturedBooks();
    }

    if (window.location.pathname.includes("orders.html")) {
        loadOrders();
    }
});

// ================= USER MENU (DROPDOWN) =================
function getStoredUser() {
    try {
        const token = getStoredToken();
        if (!token || !isTokenLikelyValid(token)) {
            // If token is missing/invalid/expired, treat as logged out.
            localStorage.removeItem("token");
            localStorage.removeItem("user");
            return null;
        }

        const raw = localStorage.getItem("user");
        if (!raw) return null;
        const user = JSON.parse(raw);
        if (!user || typeof user !== "object") return null;
        if (!user.username || !user.email || !user.role) return null;
        return user;
    } catch {
        return null;
    }
}

function getStoredToken() {
    const raw = localStorage.getItem("token");
    if (!raw || typeof raw !== "string") return null;
    const token = raw.trim();
    return token.length ? token : null;
}

function isTokenLikelyValid(token) {
    // Very small client-side check: looks like a JWT and (if exp exists) not expired.
    const payload = decodeJwtPayload(token);
    if (!payload) return false;
    if (typeof payload.exp === "number") {
        return Date.now() < payload.exp * 1000;
    }
    return true;
}

function decodeJwtPayload(token) {
    const parts = String(token).split(".");
    if (parts.length !== 3) return null;
    try {
        const base64Url = parts[1];
        const base64 = base64Url.replaceAll("-", "+").replaceAll("_", "/");
        const padded = base64.padEnd(base64.length + (4 - (base64.length % 4)) % 4, "=");
        const json = atob(padded);
        return JSON.parse(json);
    } catch {
        return null;
    }
}

function renderUserMenu() {
    const container = document.getElementById("user-greeting");
    if (!container) return;

    const user = getStoredUser();
    if (!user) {
        // Keep the default "Sign in" link if not logged in.
        return;
    }

    container.classList.add("user-menu");
    container.innerHTML = `
        <a href="#" class="nav-link user-menu-toggle" aria-haspopup="true" aria-expanded="false">
            <span class="nav-line1">Hello, ${escapeHtml(user.username)}</span>
            <span class="nav-line2">Account & Lists <i class="fas fa-caret-down"></i></span>
        </a>
        <div class="user-menu-dropdown" role="menu">
            <a href="profile.html" role="menuitem">Profile</a>
            <a href="address.html" role="menuitem">Address</a>
            <a href="orders.html" role="menuitem">Orders</a>
            <div class="user-menu-divider"></div>
            <a href="#" class="js-logout" role="menuitem">Sign out</a>
        </div>
    `;

    const toggle = container.querySelector(".user-menu-toggle");
    const dropdown = container.querySelector(".user-menu-dropdown");
    const logoutLink = container.querySelector(".js-logout");

    function closeMenu() {
        dropdown.classList.remove("show");
        toggle.setAttribute("aria-expanded", "false");
    }

    function toggleMenu() {
        const isOpen = dropdown.classList.toggle("show");
        toggle.setAttribute("aria-expanded", String(isOpen));
    }

    toggle.addEventListener("click", (e) => {
        e.preventDefault();
        e.stopPropagation();
        toggleMenu();
    });

    logoutLink.addEventListener("click", (e) => {
        e.preventDefault();
        handleLogout(e);
    });

    document.addEventListener("click", () => closeMenu());
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") closeMenu();
    });
    dropdown.addEventListener("click", (e) => e.stopPropagation());
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

// ================= EVENT LISTENERS =================
function setupEventListeners() {
    logoutBtn?.addEventListener("click", handleLogout);
    document.getElementById("registerForm")?.addEventListener("submit", handleRegister);
    document.getElementById("loginForm")?.addEventListener("submit", handleLogin);
    document.getElementById("customerLoginForm")?.addEventListener("submit", handleLogin);
    document.getElementById("sellerLoginForm")?.addEventListener("submit", handleLogin);
}

// ================= REGISTER =================
async function handleRegister(e) {
    e.preventDefault();

    const username = document.getElementById("username").value.trim();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;
    const role = document.getElementById("userRole")?.value || "BUYER";

    try {
        const endpoint = role === 'SELLER' 
            ? `${API_BASE_URL}/auth/register/seller`
            : `${API_BASE_URL}/auth/register`;

        const response = await fetch(endpoint, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, email, password })
        });

        if (!response.ok) {
            const msg = await response.text();
            throw new Error(msg);
        }

        showNotification("✅ Account created successfully!", "success");
        setTimeout(() => window.location.href = "login.html", 1500);

    } catch (err) {
        showNotification(err.message || "Registration failed", "error");
    }
}

// ================= LOGIN =================
async function handleLogin(e) {
    e.preventDefault();

    let username, password;
    
    // Check which form was submitted
    if (e.target.id === 'customerLoginForm') {
        username = document.getElementById("customerUsername").value.trim();
        password = document.getElementById("customerPassword").value;
    } else if (e.target.id === 'sellerLoginForm') {
        username = document.getElementById("sellerUsername").value.trim();
        password = document.getElementById("sellerPassword").value;
    } else {
        username = document.getElementById("username").value.trim();
        password = document.getElementById("password").value;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: "Invalid credentials" }));
            throw new Error(errorData.message || "Invalid credentials");
        }

        const data = await response.json();
        
        if (!data.token) {
            throw new Error("No token received from server");
        }
        
        const user = {
            id: data.userId,
            username: data.username,
            email: data.email,
            role: data.role
        };
        localStorage.setItem("token", data.token);
        localStorage.setItem("user", JSON.stringify(user));

        // Redirect based on role
        if (user.role === 'SELLER') {
            window.location.href = "seller-dashboard.html";
        } else {
            window.location.href = "index.html";
        }

    } catch (err) {
        showNotification(err.message || "Login failed", "error");
        console.error("Login error:", err);
    }
}

// ================= LOGOUT =================
function handleLogout(e) {
    e.preventDefault();
    localStorage.clear();
    window.location.href = "index.html";
}

// ================= LOAD BOOKS (for customers) =================
async function loadBooks(adminView = false) {
    const container = document.getElementById("booksContainer");
    if (!container) return;

    try {
        const res = await fetch(`${API_BASE_URL}/books`);
        const books = await res.json();
        renderBooks(books, container, adminView);
    } catch {
        container.innerHTML = "<p>Error loading books</p>";
    }
}

function renderBooks(books, container, adminView = false) {
    container.innerHTML = "";

    books.forEach(book => {
        const quantity = book.quantity ?? 0;
        const div = document.createElement("div");
        div.className = "book-card";
        div.innerHTML = `
            <div class="book-image">
                <img src="${book.imageUrl || 'https://via.placeholder.com/150x200?text=Book+Cover'}" 
                     alt="${book.title}">
            </div>
            <div class="book-info">
                <h3 class="book-title">${book.title}</h3>
                <p class="book-author">By ${book.author}</p>
                <p class="book-category">${book.category || ''}</p>
                <p class="book-price">$${book.price.toFixed(2)}</p>
                ${quantity > 0 
                    ? `<button class="add-to-cart" data-id="${book.id}">Add to Cart</button>`
                    : `<button class="add-to-cart" disabled>Out of Stock</button>`
                }
            </div>
        `;
        container.appendChild(div);
    });

    // Add event listeners to Add to Cart buttons
    document.querySelectorAll(".add-to-cart").forEach(btn => {
        if (!btn.disabled) {
            btn.addEventListener("click", handleAddToCart);
        }
    });
}

// ================= ADD TO CART =================
async function handleAddToCart(e) {
    const token = localStorage.getItem("token");
    const user = JSON.parse(localStorage.getItem("user"));

    if (!token || !user) {
        showNotification("Please login first", "error");
        return window.location.href = "login.html";
    }

    const bookId = e.target.dataset.id;
    
    // Simple cart implementation (in localStorage)
    let cart = JSON.parse(localStorage.getItem("cart") || "[]");
    
    // Check if book already in cart
    const existingItem = cart.find(item => item.bookId == bookId);
    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push({
            bookId: parseInt(bookId),
            quantity: 1,
            addedAt: new Date().toISOString()
        });
    }
    
    localStorage.setItem("cart", JSON.stringify(cart));
    updateCartCount();
    showNotification("📚 Book added to cart", "success");
}

// ================= UPDATE CART COUNT =================
function updateCartCount() {
    const cartCountElements = document.querySelectorAll(".cart-count");
    const cart = JSON.parse(localStorage.getItem("cart") || "[]");
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    
    cartCountElements.forEach(element => {
        element.textContent = totalItems;
    });
}

// Rest of your main.js remains the same...
// ================= FEATURED BOOKS =================
async function loadFeaturedBooks() {
    const container = document.getElementById("featuredBooks");
    if (!container) return;

    const res = await fetch(`${API_BASE_URL}/books`);
    const books = await res.json();
    renderBooks(books.slice(0, 4), container);
}

// ================= ORDERS =================
async function loadOrders() {
    const container = document.getElementById("ordersContainer");
    const token = localStorage.getItem("token");
    const user = JSON.parse(localStorage.getItem("user"));

    if (!token || !user) return;

    try {
        const res = await fetch(`${API_BASE_URL}/orders/user/${user.id}`, {
            headers: { "Authorization": `Bearer ${token}` }
        });

        if (!res.ok) throw new Error("Failed to load orders");

        const orders = await res.json();
        
        if (orders.length === 0) {
            container.innerHTML = `
                <div class="empty-orders">
                    <i class="fas fa-box-open"></i>
                    <h3>You have no orders yet</h3>
                    <p>Start shopping to see your orders here</p>
                    <a href="books.html" class="auth-button">Shop Now</a>
                </div>
            `;
            return;
        }

        container.innerHTML = orders.map(order => `
            <div class="order-item">
                <div class="order-details">
                    <div class="order-title">Order #${order.id}</div>
                    <div class="order-date">Date: ${new Date(order.orderDate).toLocaleDateString()}</div>
                    <div class="order-price">Total: $${(order.totalAmount ?? order.totalPrice ?? 0).toFixed(2)}</div>
                    <div class="order-status">Status: ${order.status}</div>
                </div>
            </div>
        `).join("");

    } catch (err) {
        container.innerHTML = `
            <div class="empty-orders">
                <i class="fas fa-exclamation-triangle"></i>
                <h3>Error loading orders</h3>
                <p>${err.message}</p>
            </div>
        `;
    }
}

// ================= CHECKOUT =================
async function checkout() {
    const token = localStorage.getItem("token");
    const user = JSON.parse(localStorage.getItem("user"));
    const cart = JSON.parse(localStorage.getItem("cart") || "[]");

    if (!token || !user) {
        showNotification("Please login first", "error");
        return window.location.href = "login.html";
    }

    if (cart.length === 0) {
        showNotification("Your cart is empty", "error");
        return;
    }

    const bookIds = cart.map(item => item.bookId);
    
    try {
        const res = await fetch(`${API_BASE_URL}/orders/place-by-ids`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({
                userId: user.id,
                bookIds: bookIds
            })
        });

        if (!res.ok) throw new Error("Checkout failed");

        // Clear cart
        localStorage.removeItem("cart");
        updateCartCount();
        
        showNotification("✅ Order placed successfully!", "success");
        setTimeout(() => window.location.href = "orders.html", 2000);

    } catch (err) {
        showNotification(err.message, "error");
    }
}

// ================= NOTIFICATIONS =================
function showNotification(msg, type) {
    const div = document.createElement("div");
    div.className = `notification ${type}`;
    div.textContent = msg;
    document.body.appendChild(div);

    setTimeout(() => div.classList.add("show"), 50);
    setTimeout(() => div.remove(), 3000);
}

// Initialize cart count on page load
updateCartCount();