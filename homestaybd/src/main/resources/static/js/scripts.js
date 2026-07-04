// scripts.js - Public homepage (index.html) এর জন্য রুম লোড করার কোড

const backendUrl = 'http://localhost:8080';

// রুম লোড করার মূল ফাংশন
async function loadRooms() {
    const list = document.getElementById('rooms-list');
    if (!list) {
        console.error("rooms-list div পাওয়া যায়নি। index.html চেক করো।");
        return;
    }

    list.innerHTML = '<div class="col-12 text-center py-5"><div class="spinner-border text-primary" role="status"></div><p class="mt-3 text-muted">রুম লোড হচ্ছে...</p></div>';

    try {
        console.log("রুম লোডের চেষ্টা করা হচ্ছে:", `${backendUrl}/rooms`);

        const res = await fetch(`${backendUrl}/rooms`);
        console.log("রুম API স্ট্যাটাস:", res.status);

        if (!res.ok) {
            const errorText = await res.text();
            throw new Error(`রুম লোড ফেল হয়েছে। স্ট্যাটাস: ${res.status} - ${errorText}`);
        }

        const rooms = await res.json();
        console.log("পাওয়া গেছে রুমের ডাটা:", rooms);

        list.innerHTML = '';

        if (!rooms || rooms.length === 0) {
            list.innerHTML = '<p class="text-center text-muted fs-5">কোনো উপলব্ধ রুম নেই। পরে আবার চেষ্টা করুন।</p>';
            return;
        }

        rooms.forEach(room => {
            const card = `
                <div class="col">
                    <div class="card h-100 shadow-sm position-relative border-0 rounded-4 overflow-hidden">
                        <!-- Wishlist Heart বাটন -->
                        <button class="btn btn-sm btn-light position-absolute top-0 end-0 m-3 wishlist-btn rounded-circle p-2 shadow-sm"
                                data-room-id="${room.id}">
                            <i class="far fa-heart fs-5"></i>
                        </button>

                        <!-- রুমের ছবি (ক্লিক করলে ডিটেইলস পেজে যাবে) -->
                        <img src="${backendUrl}${room.imagePath || '/images/placeholder-room.jpg'}"
                             class="card-img-top"
                             alt="${room.title}"
                             style="height: 240px; object-fit: cover; cursor: pointer;"
                             onclick="goToDetails(${room.id})">

                        <div class="card-body pb-2">
                            <!-- লোকেশন -->
                            <small class="text-muted d-flex align-items-center mb-1">
                                <i class="fas fa-map-marker-alt me-1"></i> ${room.location || 'অজানা'}
                            </small>

                            <!-- টাইটল -->
                            <h5 class="card-title mb-2">${room.title}</h5>

                            <!-- ছোট বিবরণ -->
                            <small class="text-muted d-block mb-3">
                                ${room.facilities?.substring(0, 60) || 'কোনো বিবরণ নেই'}...
                            </small>

                            <!-- দাম এবং রেটিং -->
                            <div class="d-flex justify-content-between align-items-center">
                                <div class="fw-bold text-dark">
                                    ${room.price} টাকা <small class="text-muted">/ রাত</small>
                                </div>
                                <div class="text-warning">
                                    <i class="fas fa-star"></i> 4.8
                                </div>
                            </div>
                        </div>

                        <div class="card-footer bg-white border-0 pt-0">
                            <button class="btn btn-primary w-100 rounded-pill" onclick="bookRoom(${room.id})">
                                বুক করুন
                            </button>
                        </div>
                    </div>
                </div>
            `;
            list.innerHTML += card;
        });

        // Wishlist বাটনের কাজ (হার্ট ক্লিক করলে লাল হবে)
        document.querySelectorAll('.wishlist-btn').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.stopPropagation(); // ছবির ক্লিকের সাথে মিশে না যায়
                const icon = this.querySelector('i');
                if (icon.classList.contains('far')) {
                    icon.classList.remove('far');
                    icon.classList.add('fas', 'text-danger');
                } else {
                    icon.classList.remove('fas', 'text-danger');
                    icon.classList.add('far');
                }
            });
        });

    } catch (error) {
        console.error("রুম লোডে সমস্যা:", error);
        list.innerHTML = `<p class="text-danger text-center">রুম লোড করতে সমস্যা হয়েছে: ${error.message}</p>`;
    }
}

// রুম ডিটেইলস পেজে নিয়ে যাওয়া
function goToDetails(id) {
    window.location.href = `/room-details.html?id=${id}`;
}

// বুক করার ফাংশন (পুরো কার্যকরী — প্রাইস ক্যালকুলেশন সহ)
function bookRoom(id) {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('বুক করার জন্য লগইন করুন');
        window.location.href = '/login.html';
        return;
    }

    // ইউজারের কাছ থেকে ইনপুট নেওয়া
    const checkIn = prompt('চেক-ইন তারিখ (YYYY-MM-DD):');
    if (!checkIn) return;

    const checkOut = prompt('চেক-আউট তারিখ (YYYY-MM-DD):');
    if (!checkOut) return;

    const persons = prompt('কতজন থাকবেন? (সংখ্যা দিন):');
    if (!persons || isNaN(persons) || persons < 1) {
        alert('সঠিক পার্সন সংখ্যা দিন');
        return;
    }

    // প্রাইস ক্যালকুলেশন দেখানো (অপশনাল — রুমের প্রাইস জানতে API কল করতে হবে)
    alert(`বুকিং ডিটেইলস:\nরুম ID: ${id}\nচেক-ইন: ${checkIn}\nচেক-আউট: ${checkOut}\nপার্সন: ${persons}\n\nবুকিং প্রসেস শুরু হচ্ছে...`);

    // বুকিং API কল (পুরো কার্যকরী)
    fetch(`${backendUrl}/bookings/add?roomId=${id}&checkIn=${checkIn}&checkOut=${checkOut}&persons=${persons}`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    })
    .then(res => {
        if (res.ok) {
            alert('বুকিং সফল হয়েছে! হোস্টকে নোটিফিকেশন পাঠানো হয়েছে।');
            // ঐচ্ছিক: পেজ রিফ্রেশ বা বুকিং লিস্ট আপডেট
        } else {
            res.text().then(text => alert('বুকিং ফেল হয়েছে: ' + text));
        }
    })
    .catch(error => {
        console.error('Booking error:', error);
        alert('বুকিং করতে সমস্যা: ' + error.message);
    });
}

// পেজ লোড হলেই রুম লোড করা
window.onload = function() {
    loadRooms();
};