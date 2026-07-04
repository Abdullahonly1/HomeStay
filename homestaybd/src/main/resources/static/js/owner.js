// owner.js - FINAL FIXED VERSION (Listings tab dynamic load + auto refresh after add)

const backendUrl = 'http://localhost:8080';

console.log("owner.js LOADED – FINAL FIXED VERSION");

// Token চেক + লগইন না থাকলে রিডাইরেক্ট
let token = localStorage.getItem('token');
console.log("Token exists?", !!token);
if (token) {
    console.log("Token preview:", token.substring(0, 40) + "...");
} else {
    console.warn("NO TOKEN – redirecting to login");
    // window.location.href = '/login.html'; // uncomment করো যদি চাও
}

// ================================================
// Image Preview + File name show
document.addEventListener('change', e => {
    if (e.target.name === 'images') {
        const preview = document.getElementById('multi-image-preview');
        if (preview) {
            preview.innerHTML = '';
            [...e.target.files].forEach((file, index) => {
                const reader = new FileReader();
                reader.onload = ev => {
                    const div = document.createElement('div');
                    div.style.margin = '10px';
                    div.style.textAlign = 'center';

                    const img = document.createElement('img');
                    img.src = ev.target.result;
                    img.style.maxWidth = '140px';
                    img.style.borderRadius = '8px';
                    img.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)';

                    const name = document.createElement('p');
                    name.textContent = file.name;
                    name.style.fontSize = '12px';
                    name.style.marginTop = '5px';
                    name.style.wordBreak = 'break-all';

                    div.appendChild(img);
                    div.appendChild(name);
                    preview.appendChild(div);
                };
                reader.readAsDataURL(file);
            });
        }
    }
});

// ================================================
// Listings Tab Setup
function setupListingsTab() {
    console.log("[setupListingsTab] Running – token:", !!token);

    const container = document.getElementById('my-rooms-list');
    if (!container) {
        console.error("my-rooms-list container not found");
        return;
    }

    if (!token) {
        container.innerHTML = `
            <div class="text-center py-5">
                <h5 class="text-danger">লগইন করা নেই</h5>
                <p>রুম দেখতে বা যোগ করতে লগইন করুন</p>
                <a href="/login.html" class="btn btn-primary mt-3">লগইন করুন</a>
            </div>`;
        return;
    }

    // প্রথমবার লোড
    loadMyRooms();

    // Add room form submit
    const form = document.getElementById('add-room-form');
    if (form) {
        console.log("[setupListingsTab] Add form found – attaching submit handler");

        form.onsubmit = async function(e) {
            e.preventDefault();
            console.log("=== রুম যোগ করুন BUTTON CLICKED ===");

            const formData = new FormData(this);

            try {
                const res = await fetch(`${backendUrl}/api/rooms/add`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`
                        // Content-Type দরকার নেই – FormData অটো সেট করে
                    },
                    body: formData
                });

                const text = await res.text();
                console.log("Add Room Response:", res.status, text);

                if (res.ok) {
                    alert('রুম সফলভাবে যোগ হয়েছে!');
                    form.reset();
                    document.getElementById('multi-image-preview')?.innerHTML = '';

                    // অটো রিফ্রেশ – লিস্টে নতুন রুম দেখাবে
                    loadMyRooms();
                } else {
                    alert(`সমস্যা (${res.status}): ${text || 'কোনো তথ্য পাওয়া যায়নি'}`);
                }
            } catch (err) {
                console.error("Submit error:", err);
                alert('সার্ভারে সমস্যা হয়েছে। পরে চেষ্টা করুন।');
            }
        };
    } else {
        console.warn("[setupListingsTab] Add form not found yet");
    }
}

// Bootstrap tab shown event
const listingsTab = document.querySelector('a[href="#listings"]');
if (listingsTab) {
    listingsTab.addEventListener('shown.bs.tab', () => {
        console.log("Listings tab shown – running setup");
        setupListingsTab();
    });
}

// যদি পেজ লোডের সময় tab active থাকে
if (document.querySelector('#listings.active')) {
    console.log("Listings tab active on load – setup after delay");
    setTimeout(setupListingsTab, 800); // delay দিলাম যাতে DOM লোড হয়
}

// MutationObserver – যদি tab dynamically load হয়
const observer = new MutationObserver(() => {
    if (document.getElementById('add-room-form') && !document.querySelector('#add-room-form[data-setup-done]')) {
        console.log("Add form detected via observer – setup");
        document.querySelector('#add-room-form').setAttribute('data-setup-done', 'true');
        setupListingsTab();
    }
});
observer.observe(document.body, { childList: true, subtree: true });

// ================================================
// loadMyRooms – improved with better UI states
async function loadMyRooms() {
    console.log("[loadMyRooms] Starting – token:", !!token);

    const container = document.getElementById('my-rooms-list');
    if (!container) return console.error("my-rooms-list container not found");

    container.innerHTML = `
        <div class="text-center py-5">
            <div class="spinner-border text-primary" role="status"></div>
            <p class="mt-3">আপনার রুমগুলো লোড হচ্ছে...</p>
        </div>`;

    if (!token) {
        container.innerHTML = `
            <div class="text-center py-5">
                <h5 class="text-danger">লগইন করা নেই</h5>
                <a href="/login.html" class="btn btn-primary mt-3">লগইন করুন</a>
            </div>`;
        return;
    }

    try {
        const res = await fetch(`${backendUrl}/api/rooms/my`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        console.log("[loadMyRooms] Status:", res.status);

        if (res.ok) {
            const rooms = await res.json();
            console.log("[loadMyRooms] Rooms loaded:", rooms.length);

            if (rooms.length === 0) {
                container.innerHTML = `
                    <div class="text-center py-5">
                        <h5>কোনো রুম যোগ করা হয়নি</h5>
                        <p>উপরের ফর্ম দিয়ে নতুন রুম যোগ করুন</p>
                    </div>`;
                return;
            }

            container.innerHTML = '';
            rooms.forEach(room => {
                const card = `
                    <div class="col-md-4 mb-4">
                        <div class="card h-100 shadow-sm">
                            <img src="${backendUrl}${room.imagePath || '/images/placeholder-room.jpg'}"
                                 class="card-img-top" alt="${room.title}"
                                 style="height: 200px; object-fit: cover;">
                            <div class="card-body">
                                <h5 class="card-title">${room.title || 'No Title'}</h5>
                                <p class="card-text">৳${room.price || 0} / রাত</p>
                                <p class="card-text text-muted">${room.location || 'N/A'}</p>
                            </div>
                        </div>
                    </div>`;
                container.innerHTML += card;
            });
        } else if (res.status === 401 || res.status === 403) {
            container.innerHTML = `
                <div class="text-center py-5">
                    <h5 class="text-danger">অনুমতি নেই বা লগইন মেয়াদ শেষ</h5>
                    <a href="/login.html" class="btn btn-primary mt-3">আবার লগইন করুন</a>
                </div>`;
        } else {
            container.innerHTML = `<p class="text-danger text-center py-5">লোড করতে সমস্যা (${res.status})</p>`;
        }
    } catch (err) {
        console.error("[loadMyRooms] Fetch error:", err);
        container.innerHTML = `<p class="text-danger text-center py-5">নেটওয়ার্ক সমস্যা</p>`;
    }
}

console.log("owner.js INITIALIZED – Listings tab-এ ক্লিক করে দেখুন");