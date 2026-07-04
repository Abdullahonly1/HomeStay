// confirm-and-refresh.js
// এই ফাইলটা owner-dashboard.html এর মধ্যে <script src="/js/confirm-and-refresh.js"></script> দিয়ে লোড করো

// confirm করার ফাংশন
function confirmBooking(bookingId) {
    const token = localStorage.getItem('token');

    if (!token) {
        alert('লগইন করা নেই। আবার লগইন করুন।');
        return;
    }

    console.log(`Confirm করা হচ্ছে: Booking ID = ${bookingId}`);

    fetch(`/bookings/${bookingId}/confirm`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`কনফার্ম ব্যর্থ হয়েছে: ${response.status} ${response.statusText}`);
        }
        return response.json();
    })
    .then(data => {
        console.log('কনফার্ম সফল:', data);

        // ড্যাশবোর্ড আপডেট
        fetchHostSummary(token);

        alert('বুকিং সফলভাবে কনফার্ম হয়েছে! ড্যাশবোর্ড আপডেট হচ্ছে...');

        // confirm button disable করা
        const btn = document.querySelector(`[data-booking-id="${bookingId}"]`);
        if (btn) {
            btn.disabled = true;
            btn.textContent = 'Confirmed';
            btn.classList.remove('btn-success');
            btn.classList.add('btn-secondary');
        }
    })
    .catch(error => {
        console.error('কনফার্ম এরর:', error);
        alert('কনফার্ম করতে সমস্যা হয়েছে: ' + error.message);
    });
}

// fetchHostSummary ফাংশন (null-safe + optional chaining + warning)
function fetchHostSummary(token) {
    if (!token) {
        console.warn("No token for fetching host summary");
        return;
    }

    fetch('/host-summary', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Accept': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`সামারি লোড ব্যর্থ: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        console.log('নতুন সামারি ডাটা:', data);

        // Null-safe update (optional chaining + fallback)
        document.getElementById('pending-requests')?.textContent = data.pendingRequests || 0;
        document.getElementById('unread-messages')?.textContent = data.unreadMessages || 0;

        const earningsEl = document.getElementById('monthly-earnings') || document.getElementById('earnings-this-month');
        if (earningsEl) {
            const earnings = data.earningsThisMonth || 0;
            earningsEl.textContent = earnings.toLocaleString('bn-BD') + ' ৳';
        } else {
            console.warn("Earnings element not found (monthly-earnings or earnings-this-month)");
        }

        const upcomingEl = document.getElementById('upcoming-checkins');
        if (upcomingEl) {
            const upcoming = data.upcomingCheckins || 0;
            upcomingEl.textContent = upcoming;

            // আজ/কাল হাইলাইট (যদি data-তে থাকে)
            if ((data.todayCheckins || 0) > 0 || (data.tomorrowCheckins || 0) > 0) {
                upcomingEl.style.color = '#e74c3c';
                upcomingEl.title = `আজ: ${data.todayCheckins || 0}, কাল: ${data.tomorrowCheckins || 0}`;
            } else {
                upcomingEl.style.color = '';
                upcomingEl.title = '';
            }
        } else {
            console.warn("upcoming-checkins element not found");
        }

        console.log("Dashboard summary updated successfully");
    })
    .catch(error => {
        console.error('সামারি লোড এরর:', error);
        // Fallback: সব element-এ 0 দেখানো (optional)
        document.getElementById('pending-requests')?.textContent = '0';
        document.getElementById('unread-messages')?.textContent = '0';
        document.getElementById('monthly-earnings')?.textContent = '0 ৳';
        document.getElementById('upcoming-checkins')?.textContent = '0';
    });
}

// পেজ লোড হলে প্রথমবার সামারি লোড
document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    if (token) {
        console.log("Page loaded → Fetching initial host summary");
        fetchHostSummary(token);
    } else {
        console.warn("No token found on page load");
    }
});