const backendUrl = 'http://localhost:8080';

// লগইন ফর্ম
document.getElementById('login-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim(); // trim() যোগ করা হয়েছে

    if (!username || !password) {
        alert('ইউজারনেম এবং পাসওয়ার্ড দিন');
        return;
    }

    const data = { username, password };

    try {
        console.log("লগইন রিকোয়েস্ট পাঠানো হচ্ছে...", { username });

        const res = await fetch(`${backendUrl}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        console.log("লগইন রেসপন্স স্ট্যাটাস:", res.status);

        if (res.ok) {
            let token = await res.text();
            token = token.trim(); // অতিরিক্ত স্পেস/লাইন ব্রেক মুছে ফেলা

            console.log("লগইন সফল! Raw token:", token.substring(0, 40) + "...");

            localStorage.setItem('token', token);

            // ────────────── ডিবাগ চেক ──────────────
            const savedToken = localStorage.getItem('token');
            console.log("localStorage-এ সেভ হয়েছে কি?", savedToken ? "হ্যাঁ" : "না");

            if (savedToken) {
                console.log("সেভ করা টোকেন (প্রথম ৪০ অক্ষর):", savedToken.substring(0, 40) + "...");
                console.log("✅ টোকেন localStorage-এ সফলভাবে সেভ হয়েছে");
            } else {
                console.error("❌ টোকেন localStorage-এ সেভ হয়নি!");
                alert("টোকেন সেভ করতে সমস্যা হয়েছে। আবার চেষ্টা করুন।");
                return;
            }
            // ──────────────────────────────────────

            // ইউজারের রোল চেক করা
            const meRes = await fetch(`${backendUrl}/auth/me`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Accept': 'application/json'
                }
            });

            console.log("/auth/me স্ট্যাটাস:", meRes.status);

            if (meRes.ok) {
                const user = await meRes.json();
                console.log("ইউজার ডাটা:", user);

                alert(`লগইন সফল! আপনি: ${user.role}`);

                if (user.role === 'OWNER') {
                    window.location.href = '/owner-dashboard.html';
                } else if (user.role === 'USER') {
                    window.location.href = '/user-dashboard.html';
                } else {
                    alert('অজানা রোল: ' + user.role);
                    window.location.href = '/';
                }
            } else {
                const errText = await meRes.text();
                console.error("/auth/me error:", errText);
                alert('ইউজারের তথ্য লোড করতে সমস্যা: ' + errText);
                // টোকেন ইনভ্যালিড হলে মুছে দাও
                localStorage.removeItem('token');
                window.location.href = '/login.html';
            }
        } else {
            const error = await res.text();
            console.error("লগইন ফেল:", error);
            alert('লগইন ফেল: ' + error);
        }
    } catch (error) {
        console.error("লগইন প্রক্রিয়ায় সমস্যা:", error);
        alert('সার্ভার এরর: ' + error.message);
    }
});

// রেজিস্টার ফর্ম
document.getElementById('register-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    // ফিল্ডগুলো নেওয়া
    const usernameInput = document.getElementById('username');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const roleInput = document.querySelector('input[name="role"]:checked');

    // চেক করা যে সবকিছু পাওয়া গেছে কি না
    if (!usernameInput || !emailInput || !passwordInput || !roleInput) {
        alert('ফর্মে কোনো ফিল্ড মিসিং আছে। HTML চেক করুন।');
        return;
    }

    const username = usernameInput.value.trim();
    const email = emailInput.value.trim();
    const password = passwordInput.value.trim();
    const role = roleInput.value;

    // ফাঁকা চেক
    if (!username || !email || !password || !role) {
        alert('সব ফিল্ড পূরণ করুন');
        return;
    }

    const data = { username, email, password, role };

    try {
        const res = await fetch(`${backendUrl}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        const responseText = await res.text();

        if (res.ok) {
            alert('রেজিস্টার সফল! এখন লগইন করুন');
            window.location.href = '/login.html';
        } else {
            alert('রেজিস্টার ফেল: ' + responseText);
        }
    } catch (error) {
        alert('সার্ভারের সাথে সমস্যা: ' + error.message);
        console.error(error);
    }
});
// লগআউট
function logout() {
    localStorage.removeItem('token');
    alert('লগআউট সফল');
    window.location.href = '/';
}