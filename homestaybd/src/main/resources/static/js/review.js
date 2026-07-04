// review.js

// স্টার ক্লিক করে রেটিং সিলেক্ট করা
document.addEventListener('DOMContentLoaded', function() {
  const stars = document.querySelectorAll('.star-rating .star');

  stars.forEach(star => {
    star.addEventListener('click', function() {
      const value = parseInt(this.dataset.value);
      stars.forEach((s, index) => {
        if (index < value) {
          s.classList.add('active');
        } else {
          s.classList.remove('active');
        }
      });
    });
  });
});

// রিভিউ সাবমিট ফাংশন
async function submitReview() {
  const activeStars = document.querySelectorAll('.star-rating .star.active');
  const rating = activeStars.length;

  const commentElement = document.getElementById('review-text');
  const comment = commentElement ? commentElement.value.trim() : '';

  if (rating === 0) {
    alert('দয়া করে রেটিং দিন (স্টার ক্লিক করুন)');
    return;
  }

  if (!comment) {
    alert('দয়া করে কমেন্ট লিখুন');
    return;
  }

  const token = localStorage.getItem('token');
  if (!token) {
    alert('রিভিউ দিতে লগইন করতে হবে। লগইন পেজে যাচ্ছি...');
    window.location.href = '/login.html';
    return;
  }

  const reviewData = {
    roomId: parseInt(roomId),  // room-details.html এ roomId আছে
    rating: rating,
    comment: comment
  };

  console.log('Submitting review:', reviewData); // ডিবাগের জন্য

  try {
    const response = await fetch('http://localhost:8080/reviews', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(reviewData)
    });

    if (response.ok) {
      alert('রিভিউ সফলভাবে জমা হয়েছে! ধন্যবাদ।');
      if (commentElement) commentElement.value = '';
      document.querySelectorAll('.star-rating .star').forEach(s => s.classList.remove('active'));
    } else {
      let errorText = 'অজানা সমস্যা';
      try {
        errorText = await response.text();
      } catch {}
      alert(`রিভিউ জমা হয়নি: ${errorText} (Status: ${response.status})`);
    }
  } catch (error) {
    console.error('রিভিউ submit error:', error);
    alert('সার্ভারের সাথে যোগাযোগে সমস্যা। পরে আবার চেষ্টা করুন।');
  }
}