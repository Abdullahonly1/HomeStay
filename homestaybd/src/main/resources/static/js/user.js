const backendUrl = 'http://localhost:8080';

document.getElementById('booking-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const roomId = document.getElementById('roomId').value;
    const checkIn = document.getElementById('checkIn').value;
    const checkOut = document.getElementById('checkOut').value;
    const persons = document.getElementById('persons').value;

    try {
        const res = await fetch(`${backendUrl}/bookings/add?roomId=${roomId}&checkIn=${checkIn}&checkOut=${checkOut}&persons=${persons}`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });

        if (res.ok) {
            alert('বুকিং সফল! হোস্টকে নোটিফিকেশন পাঠানো হয়েছে।');
            loadMyBookings(); // রিফ্রেশ
        } else {
            alert('বুকিং ফেল: ' + await res.text());
        }
    } catch (error) {
        alert('সার্ভার সমস্যা: ' + error.message);
    }
});

// আমার বুকিং লোড
async function loadMyBookings() {
    // পরে implement করো
    alert('আমার বুকিং লোড হচ্ছে...');
}

window.onload = loadMyBookings;

function logout() {
    localStorage.removeItem('token');
    window.location.href = '/';
}