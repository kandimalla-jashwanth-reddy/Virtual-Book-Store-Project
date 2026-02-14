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
        const urlParams = new URLSearchParams(window.location.search);
        if (!urlParams.get('search')) {
            loadBooks();
        }
    }

    if (window.location.pathname.includes("index.html") || window.location.pathname === "/") {
        loadFeaturedBooks();
    }

    if (window.location.pathname.includes("orders.html")) {
        loadOrders();
    }

    if (window.location.pathname.includes("address.html")) {
        loadAddresses();
        setupAddressListeners();
    }
});

// ================= USER MENU (DROPDOWN) =================
function getStoredUser() {
    try {
        const token = getStoredToken();
        if (token && isTokenLikelyValid(token)) {
            const raw = localStorage.getItem("user");
            if (!raw) return null;
            const user = JSON.parse(raw);
            if (!user || typeof user !== "object") return null;
            if (!user.username || !user.email || !user.role) return null;
            return user;
        }
        return null;
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
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.message || "Registration failed");
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
            role: data.role ? data.role.toUpperCase() : null
        };

        const userRole = user.role;
        const isSellerLogin = e.target.id === 'sellerLoginForm';
        const isCustomerLogin = e.target.id === 'customerLoginForm';

        console.log("Login Role Check:", { userRole, isSellerLogin, isCustomerLogin });

        if (isSellerLogin) {
            if (userRole !== 'SELLER') {
                throw new Error("Unauthorized Access: You are not a Seller.");
            }
        }

        if (isCustomerLogin) {
            if (userRole === 'SELLER') {
                throw new Error("Unauthorized: Please use the Seller Login.");
            }
        }

        localStorage.setItem("token", data.token);
        localStorage.setItem("user", JSON.stringify(user));

        if (userRole === 'SELLER') {
            window.location.href = "seller-dashboard.html";
        } else {
            window.location.href = "Book.html";
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
                <p class="book-stock" style="font-size: 0.9em; color: ${quantity < 5 ? 'red' : 'green'};">
                    ${quantity > 0 ? `${quantity} left in stock` : 'Out of Stock'}
                </p>
                ${quantity > 0
                ? `<button class="add-to-cart" data-id="${book.id}">Add to Cart</button>`
                : `<button class="add-to-cart" disabled>Out of Stock</button>`
            }
            </div>
        `;
        container.appendChild(div);
    });

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

    try {
        const res = await fetch(`${API_BASE_URL}/books/${bookId}`);
        if (!res.ok) throw new Error("Failed to fetch book info");
        const book = await res.json();

        let cart = JSON.parse(localStorage.getItem("cart") || "[]");
        const existingItem = cart.find(item => item.bookId == bookId);
        let currentQtyInCart = existingItem ? existingItem.quantity : 0;

        if (currentQtyInCart + 1 > book.quantity) {
            showNotification(`Cannot add more. Only ${book.quantity} available.`, "error");
            return;
        }

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

    } catch (err) {
        showNotification(err.message, "error");
    }
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
                    <div class="order-status">Status: <strong>${order.status}</strong></div>
                    ${order.status === 'PLACED' || order.status === 'PENDING' ?
                `<button onclick="requestCancelOrder(${order.id})" class="remove-btn" style="margin-top:10px;">Cancel Order</button>`
                : ''}
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

updateCartCount();

// ================= ADDRESS MANAGEMENT =================
function setupAddressListeners() {
    const addBtn = document.getElementById("addAddressBtn");
    const cancelBtn = document.getElementById("cancelAddressBtn");
    const form = document.getElementById("addressForm");
    const modal = document.getElementById("address-form-modal");

    if (addBtn) {
        addBtn.addEventListener("click", () => {
            document.getElementById("formTitle").textContent = "Add New Address";
            document.getElementById("addressId").value = "";
            form.reset();
            modal.style.display = "flex";
        });
    }

    if (cancelBtn) {
        cancelBtn.addEventListener("click", () => {
            modal.style.display = "none";
        });
    }

    if (form) {
        form.addEventListener("submit", handleSaveAddress);
    }

    if (modal) {
        modal.addEventListener("click", (e) => {
            if (e.target === modal) {
                modal.style.display = "none";
            }
        });
    }
}

async function loadAddresses() {
    const container = document.getElementById("address-list");
    const user = JSON.parse(localStorage.getItem("user"));
    const token = localStorage.getItem("token");

    if (!user || !token) {
        window.location.href = "login.html";
        return;
    }

    try {
        const res = await fetch(`${API_BASE_URL}/addresses/user/${user.id}`, {
            headers: { "Authorization": `Bearer ${token}` }
        });

        if (!res.ok) throw new Error("Failed to load addresses");

        const addresses = await res.json();
        renderAddresses(addresses, container);

    } catch (err) {
        console.error(err);
        container.innerHTML = `<p>Error loading addresses: ${err.message}</p>`;
    }
}

function renderAddresses(addresses, container) {
    if (addresses.length === 0) {
        container.innerHTML = `
            <div style="grid-column: 1/-1; text-align: center; padding: 40px; background: white; border-radius: 8px;">
                <p>No addresses found. Add a new one!</p>
            </div>`;
        return;
    }

    container.innerHTML = addresses.map(addr => `
        <div class="address-card" style="background: white; padding: 20px; border-radius: 8px; border: 1px solid #eee; position: relative;">
            <div class="address-actions" style="position: absolute; top: 15px; right: 15px; display: flex; gap: 10px;">
                <button onclick="editAddress(${addr.id})" style="background: none; border: none; color: #007bff; cursor: pointer;">
                    <i class="fas fa-edit"></i> Edit
                </button>
                <button onclick="deleteAddress(${addr.id})" style="background: none; border: none; color: #cc0000; cursor: pointer;">
                    <i class="fas fa-trash"></i> Delete
                </button>
            </div>
            <h3 style="margin-bottom: 10px;">${escapeHtml(addr.fullName)}</h3>
            <p>${escapeHtml(addr.streetAddress)}</p>
            <p>${escapeHtml(addr.city)}, ${escapeHtml(addr.state)} ${escapeHtml(addr.zipCode)}</p>
            <p>${escapeHtml(addr.country)}</p>
            <p style="margin-top: 10px; color: #555;">Phone: ${escapeHtml(addr.phoneNumber)}</p>
        </div>
    `).join("");

    window.currentAddresses = addresses;
}

async function handleSaveAddress(e) {
    e.preventDefault();

    const user = JSON.parse(localStorage.getItem("user"));
    const token = localStorage.getItem("token");
    const addressId = document.getElementById("addressId").value;

    const addressData = {
        fullName: document.getElementById("fullName").value,
        phoneNumber: document.getElementById("phoneNumber").value,
        streetAddress: document.getElementById("streetAddress").value,
        city: document.getElementById("city").value,
        state: document.getElementById("state").value,
        zipCode: document.getElementById("zipCode").value,
        country: document.getElementById("country").value,
        user: { id: user.id }
    };

    try {
        const method = addressId ? "PUT" : "POST";
        const url = addressId
            ? `${API_BASE_URL}/addresses/${addressId}`
            : `${API_BASE_URL}/addresses/user/${user.id}`;

        const res = await fetch(url, {
            method: method,
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(addressData)
        });

        if (!res.ok) throw new Error("Failed to save address");

        document.getElementById("address-form-modal").style.display = "none";
        showNotification("Address saved successfully!", "success");
        loadAddresses(); // Reload list

    } catch (err) {
        showNotification(err.message, "error");
    }
}

window.editAddress = function (id) {
    const address = window.currentAddresses.find(a => a.id === id);
    if (!address) return;

    document.getElementById("formTitle").textContent = "Edit Address";
    document.getElementById("addressId").value = address.id;
    document.getElementById("fullName").value = address.fullName;
    document.getElementById("phoneNumber").value = address.phoneNumber;
    document.getElementById("streetAddress").value = address.streetAddress;
    document.getElementById("city").value = address.city;
    document.getElementById("state").value = address.state;
    document.getElementById("zipCode").value = address.zipCode;
    document.getElementById("country").value = address.country;

    document.getElementById("address-form-modal").style.display = "flex";
}

window.deleteAddress = async function (id) {
    if (!confirm("Are you sure you want to delete this address?")) return;

    const token = localStorage.getItem("token");
    try {
        const res = await fetch(`${API_BASE_URL}/addresses/${id}`, {
            method: "DELETE",
            headers: { "Authorization": `Bearer ${token}` }
        });

        if (!res.ok) throw new Error("Failed to delete address");

        showNotification("Address deleted", "success");
        loadAddresses();
    } catch (err) {
        showNotification(err.message, "error");
    }
}

// ================= FORGOT PASSWORD =================
function setupForgotPassword() {
    const modal = document.getElementById("forgotPasswordModal");
    const link = document.getElementById("forgotPasswordLink");
    const closeBtn = document.getElementById("closeForgotModal");
    const sendOtpBtn = document.getElementById("sendOtpBtn");
    const resetPasswordBtn = document.getElementById("resetPasswordBtn");
    const togglePassword = document.getElementById("togglePassword");
    const newPassword = document.getElementById("newPassword");

    if (link && modal) {
        link.addEventListener("click", (e) => {
            e.preventDefault();
            modal.style.display = "flex";
            document.getElementById("forgotStep1").style.display = "block";
            document.getElementById("forgotStep2").style.display = "none";
            document.getElementById("forgotMessage").textContent = "";
            if (newPassword) {
                newPassword.value = "";
                newPassword.type = "password";
            }
            if (togglePassword) togglePassword.classList.replace("fa-eye-slash", "fa-eye");
        });
    }

    if (closeBtn && modal) {
        closeBtn.addEventListener("click", () => {
            modal.style.display = "none";
        });
    }

    if (window) {
        window.addEventListener("click", (e) => {
            if (e.target === modal) {
                modal.style.display = "none";
            }
        });
    }

    if (sendOtpBtn) {
        sendOtpBtn.addEventListener("click", handleSendOtp);
    }

    if (resetPasswordBtn) {
        resetPasswordBtn.addEventListener("click", handleResetPassword);
    }

    if (togglePassword && newPassword) {
        togglePassword.addEventListener("click", () => {
            const type = newPassword.getAttribute("type") === "password" ? "text" : "password";
            newPassword.setAttribute("type", type);

            if (type === "text") {
                togglePassword.classList.remove("fa-eye");
                togglePassword.classList.add("fa-eye-slash");
            } else {
                togglePassword.classList.remove("fa-eye-slash");
                togglePassword.classList.add("fa-eye");
            }
        });
    }
}

async function handleSendOtp() {
    const email = document.getElementById("forgotEmail").value.trim();
    const msgDiv = document.getElementById("forgotMessage");

    if (!email) {
        msgDiv.textContent = "Please enter your email.";
        return;
    }

    msgDiv.textContent = "Sending OTP...";
    msgDiv.style.color = "blue";

    try {
        const response = await fetch(`${API_BASE_URL}/auth/forgot-password`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email: email })
        });

        if (response.ok) {
            msgDiv.textContent = "OTP sent to your email.";
            msgDiv.style.color = "green";
            document.getElementById("forgotStep1").style.display = "none";
            document.getElementById("forgotStep2").style.display = "block";
        } else {
            const data = await response.text();
            throw new Error(data);
        }
    } catch (err) {
        msgDiv.textContent = err.message || "Failed to send OTP.";
        msgDiv.style.color = "red";
    }
}

async function handleResetPassword() {
    const email = document.getElementById("forgotEmail").value.trim();
    const otp = document.getElementById("forgotOtp").value.trim();
    const newPassword = document.getElementById("newPassword").value;
    const msgDiv = document.getElementById("forgotMessage");

    if (!otp || !newPassword) {
        msgDiv.textContent = "Please fill in all fields.";
        return;
    }

    msgDiv.textContent = "Resetting password...";
    msgDiv.style.color = "blue";

    try {
        const response = await fetch(`${API_BASE_URL}/auth/reset-password`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                email: email,
                otp: otp,
                newPassword: newPassword
            })
        });

        if (response.ok) {
            msgDiv.textContent = "Password reset successfully! You can now login.";
            msgDiv.style.color = "green";
            setTimeout(() => {
                document.getElementById("forgotPasswordModal").style.display = "none";
            }, 2000);
        } else {
            const data = await response.text();
            throw new Error(data);
        }
    } catch (err) {
        msgDiv.textContent = err.message || "Failed to reset password.";
        msgDiv.style.color = "red";
    }
}


// ================= ORDER & COUPON LOGIC =================
window.pendingOrder = {
    subtotal: 0,
    discount: 0,
    total: 0,
    couponCode: null
};

async function loadOrderSummary() {
    const container = document.getElementById("orderItemsSummary");
    if (!container) return;

    const cart = JSON.parse(localStorage.getItem("cart") || "[]");
    if (cart.length === 0) {
        window.location.href = "cart.html";
        return;
    }

    let itemsHtml = "";
    let subtotal = 0;

    for (const item of cart) {
        try {
            const res = await fetch(`${API_BASE_URL}/books/${item.bookId}`);
            if (!res.ok) continue;
            const book = await res.json();
            const total = book.price * item.quantity;
            subtotal += total;

            itemsHtml += `
            <div class="summary-item" style="border-bottom: 1px solid #f0f0f0; padding: 5px 0;">
                <span>${book.title} (x${item.quantity})</span>
                <span>$${total.toFixed(2)}</span>
            </div>
            `;
        } catch (e) { console.error(e); }
    }

    container.innerHTML = itemsHtml;

    window.pendingOrder.subtotal = subtotal;
    window.pendingOrder.total = subtotal; // + tax/shipping if needed
    updateOrderDisplay();
}

function updateOrderDisplay() {
    document.getElementById("subtotalDisplay").textContent = `$${window.pendingOrder.subtotal.toFixed(2)}`;
    document.getElementById("discountDisplay").textContent = `-$${window.pendingOrder.discount.toFixed(2)}`;
    document.getElementById("totalDisplay").textContent = `$${window.pendingOrder.total.toFixed(2)}`;
}

async function applyCoupon() {
    const code = document.getElementById("couponCode").value.trim();
    const msg = document.getElementById("couponMessage");

    if (!code) {
        msg.textContent = "Please enter a code";
        msg.style.color = "red";
        return;
    }

    try {
        const token = getStoredToken();
        const headers = {};
        if (token) headers["Authorization"] = `Bearer ${token}`;
        const res = await fetch(`${API_BASE_URL}/coupons/validate?code=${encodeURIComponent(code)}&amount=${window.pendingOrder.subtotal}`, { headers });
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.error || "Invalid coupon");
        }

        const coupon = await res.json();

        let discount = 0;
        if (coupon.discountPercentage) {
            discount = (window.pendingOrder.subtotal * coupon.discountPercentage) / 100;
        }

        window.pendingOrder.discount = discount;
        window.pendingOrder.total = window.pendingOrder.subtotal - discount;
        window.pendingOrder.couponCode = code;

        updateOrderDisplay();

        msg.textContent = `Coupon ${code} applied!`;
        msg.style.color = "green";

    } catch (err) {
        msg.textContent = err.message;
        msg.style.color = "red";
        window.pendingOrder.discount = 0;
        window.pendingOrder.total = window.pendingOrder.subtotal;
        window.pendingOrder.couponCode = null;
        updateOrderDisplay();
    }
}

function proceedToPayment() {
    localStorage.setItem("pendingOrder", JSON.stringify(window.pendingOrder));
    window.location.href = "payment.html";
}

async function finishPayment() {
    const token = localStorage.getItem("token");
    const user = JSON.parse(localStorage.getItem("user"));
    const cart = JSON.parse(localStorage.getItem("cart") || "[]");
    const pending = JSON.parse(localStorage.getItem("pendingOrder") || "{}");

    if (!token || !user) return window.location.href = "login.html";

    const items = [];
    for (const cartItem of cart) {
        const res = await fetch(`${API_BASE_URL}/books/${cartItem.bookId}`);
        const book = await res.json();
        items.push({
            book: book,
            quantity: cartItem.quantity,
            price: book.price * cartItem.quantity
        });
    }

    const orderData = {
        user: { id: user.id },
        items: items,
        couponCode: pending.couponCode,
        discountAmount: pending.discount,
        totalAmount: pending.total,
        status: "PLACED"
    };

    try {
        const res = await fetch(`${API_BASE_URL}/orders/place`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(orderData)
        });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.error || "Order failed");
        }

        localStorage.removeItem("cart");
        localStorage.removeItem("pendingOrder");
        updateCartCount();

        showNotification("Payment Successful! Order Placed.", "success");
        setTimeout(() => window.location.href = "orders.html", 2000);

    } catch (err) {
        showNotification(err.message, "error");
    }
}

// ================= CANCEL ORDER =================
async function requestCancelOrder(orderId) {
    const reason = prompt("Please enter a reason for cancellation:");
    if (!reason) return;

    const token = localStorage.getItem("token");
    try {
        const res = await fetch(`${API_BASE_URL}/orders/${orderId}/cancel`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({ reason: reason })
        });

        if (!res.ok) throw new Error("Failed to cancel order");

        showNotification("Order cancelled successfully", "success");
        setTimeout(() => location.reload(), 1500);

    } catch (err) {
        showNotification(err.message, "error");
    }
}

// ================= RAZORPAY PAYMENT LOGIC =================

async function startPayment() {
    const token = localStorage.getItem("token");
    const user = JSON.parse(localStorage.getItem("user"));
    const pending = JSON.parse(localStorage.getItem("pendingOrder") || "{}");

    if (!token || !user) return window.location.href = "login.html";

    try {
        const res = await fetch(`${API_BASE_URL}/payment/create-order`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({ amount: pending.total })
        });

        if (!res.ok) throw new Error("Failed to create payment order");
        const orderData = await res.json();
        let rzpOrder = orderData;
        if (typeof orderData === 'string') {
            rzpOrder = JSON.parse(orderData);
        }

        const options = {
            "key": "YOUR_RAZORPAY_KEY_ID",
            "amount": rzpOrder.amount,
            "currency": "INR",
            "name": "Virtual Bookstore",
            "description": "Book Purchase",
            "image": "images/logo.png",
            "order_id": rzpOrder.id,
            "handler": function (response) {
                verifyPayment(response, pending);
            },
            "prefill": {
                "name": user.username,
                "email": user.email,
                "contact": "9999999999"
            },
            "theme": {
                "color": "#3399cc"
            }
        };

        const rzp1 = new Razorpay(options);
        rzp1.on('payment.failed', function (response) {
            showNotification("Payment Failed: " + response.error.description, "error");
        });
        rzp1.open();

    } catch (err) {
        console.error(err);
        showNotification("Payment initiation failed", "error");
    }
}

async function verifyPayment(paymentResponse, pendingOrderData) {
    const token = localStorage.getItem("token");

    try {
        const verifyRes = await fetch(`${API_BASE_URL}/payment/verify`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({
                razorpay_order_id: paymentResponse.razorpay_order_id,
                razorpay_payment_id: paymentResponse.razorpay_payment_id,
                razorpay_signature: paymentResponse.razorpay_signature
            })
        });

        if (!verifyRes.ok) throw new Error("Payment verification failed");

        // calls the existing finishPayment logic but with updated payment details if needed
        // For now, call finishPayment which places the order in DB
        // We might want to pass payment ID to finishPayment to save it, but Order entity has fields now.

        // Let's call a modified finishPayment that includes payment IDs
        await placeOrderWithPayment(paymentResponse.razorpay_order_id, paymentResponse.razorpay_payment_id, paymentResponse.razorpay_signature);

    } catch (err) {
        showNotification(err.message, "error");
    }
}

async function placeOrderWithPayment(rzpOrderId, rzpPaymentId, signature) {
    const token = localStorage.getItem("token");
    const user = JSON.parse(localStorage.getItem("user"));
    const cart = JSON.parse(localStorage.getItem("cart") || "[]");
    const pending = JSON.parse(localStorage.getItem("pendingOrder") || "{}");

    const items = [];
    for (const cartItem of cart) {
        const res = await fetch(`${API_BASE_URL}/books/${cartItem.bookId}`);
        const book = await res.json();
        items.push({
            book: { id: book.id },
            quantity: cartItem.quantity,
            price: book.price * cartItem.quantity
        });
    }

    const orderData = {
        user: { id: user.id },
        items: items,
        couponCode: pending.couponCode,
        discountAmount: pending.discount,
        totalAmount: pending.total,
        status: "PLACED",
        razorpayOrderId: rzpOrderId,
        razorpayPaymentId: rzpPaymentId,
        paymentSignature: signature
    };

    const res = await fetch(`${API_BASE_URL}/orders/place`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify(orderData)
    });

    if (!res.ok) throw new Error("Order placement failed");

    localStorage.removeItem("cart");
    localStorage.removeItem("pendingOrder");
    updateCartCount();

    showNotification("Payment Successful! Order Placed.", "success");
    setTimeout(() => window.location.href = "orders.html", 2000);
}


window.loadOrderSummary = loadOrderSummary;
window.applyCoupon = applyCoupon;
window.proceedToPayment = proceedToPayment;
window.finishPayment = finishPayment;
window.startPayment = startPayment;
window.requestCancelOrder = requestCancelOrder;
window.setupForgotPassword = setupForgotPassword;
