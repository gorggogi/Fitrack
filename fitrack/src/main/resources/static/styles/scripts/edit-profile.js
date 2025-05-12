document.addEventListener('DOMContentLoaded', function() {
    const profilePictureInput = document.getElementById('profilePicture');
    const profilePicturePreview = document.getElementById('profilePicturePreview');

    if (profilePictureInput && profilePicturePreview) {
        profilePictureInput.addEventListener('change', function(event) {
            const file = event.target.files[0];
            if (file && file.type.startsWith('image/')) {
                const reader = new FileReader();
                
                reader.onload = function(e) {
                    profilePicturePreview.src = e.target.result;
                }
                
                reader.readAsDataURL(file);
            } else {
                // Optional: Handle non-image file selection if needed
                // e.g., reset to a default image or the original image.
                // For now, it does nothing if a non-image is selected.
            }
        });
    }

    // Email verification logic
    const emailInput = document.getElementById('email');
    const sendBtn = document.getElementById('sendVerificationBtn');
    const verificationSection = document.getElementById('verificationSection');
    const verifyBtn = document.getElementById('verifyCodeBtn');
    const verificationCodeInput = document.getElementById('verificationCode');
    const statusSpan = document.getElementById('emailVerificationStatus');
    const mainForm = document.getElementById('editProfileForm'); // Get the form itself
    const csrfTokenInput = mainForm.querySelector('input[name="_csrf"]'); 
    const containerDiv = document.querySelector('.container'); // Get the container with the data attribute
    let originalEmail = containerDiv ? containerDiv.getAttribute('data-original-email') : emailInput.value;
    let emailVerified = true; // Assume email is initially verified unless changed

    function updateEmailVerificationState() {
        const currentEmail = emailInput.value.trim();
        if (currentEmail !== originalEmail && currentEmail !== '') {
            sendBtn.style.display = 'inline-block'; // Show 'Send Code' button
            verificationSection.style.display = 'none'; // Hide verification input
            statusSpan.textContent = 'Verification needed for new email.';
            statusSpan.className = 'verification-status warning';
            emailVerified = false;
        } else {
            sendBtn.style.display = 'none'; // Hide button if email matches original or is empty
            verificationSection.style.display = 'none';
            statusSpan.textContent = ''; // Clear status
            statusSpan.className = 'verification-status';
            emailVerified = true;
        }
    }

    if (emailInput && sendBtn && verificationSection && verifyBtn && statusSpan && csrfTokenInput) {
        // Initial check on page load
        updateEmailVerificationState(); 

        // Check on email input change
        emailInput.addEventListener('input', updateEmailVerificationState);

        // --- Send Verification Code Button --- 
        sendBtn.addEventListener('click', function() {
            const newEmail = emailInput.value.trim();
            if (newEmail === '' || newEmail === originalEmail) {
                statusSpan.textContent = 'Email has not changed or is empty.';
                statusSpan.className = 'verification-status error';
                return;
            }

            sendBtn.disabled = true;
            statusSpan.textContent = 'Sending code...';
            statusSpan.className = 'verification-status info';

            fetch('/user/profile/send-verification', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-CSRF-TOKEN': csrfTokenInput.value
                },
                body: `newEmail=${encodeURIComponent(newEmail)}`
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    verificationSection.style.display = 'block'; // Show code input
                    sendBtn.style.display = 'none'; // Hide send button
                    statusSpan.textContent = data.message;
                    statusSpan.className = 'verification-status success';
                    emailVerified = false; // Explicitly mark as unverified
                } else {
                    statusSpan.textContent = data.message || 'Failed to send code.';
                    statusSpan.className = 'verification-status error';
                    sendBtn.disabled = false; // Re-enable button on failure
                }
            })
            .catch(error => {
                console.error('Error sending verification code:', error);
                statusSpan.textContent = 'Error sending code. Check console.';
                statusSpan.className = 'verification-status error';
                sendBtn.disabled = false; // Re-enable button on error
            });
        });

        // --- Verify Code Button --- 
        verifyBtn.addEventListener('click', function() {
            const code = verificationCodeInput.value.trim();
            if (code === '') {
                statusSpan.textContent = 'Please enter the verification code.';
                statusSpan.className = 'verification-status error';
                return;
            }

            verifyBtn.disabled = true;
            statusSpan.textContent = 'Verifying code...';
            statusSpan.className = 'verification-status info';

            fetch('/user/profile/verify-email-change', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-CSRF-TOKEN': csrfTokenInput.value
                },
                body: `code=${encodeURIComponent(code)}`
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    verificationSection.style.display = 'none'; // Hide code input section
                    statusSpan.textContent = data.message;
                    statusSpan.className = 'verification-status success';
                    // Update the email input value and the 'original' email state
                    emailInput.value = data.newEmail; 
                    originalEmail = data.newEmail;
                    emailVerified = true; // Mark as verified
                    updateEmailVerificationState(); // Refresh button/status state
                } else {
                    statusSpan.textContent = data.message || 'Invalid or expired code.';
                    statusSpan.className = 'verification-status error';
                    verifyBtn.disabled = false; // Re-enable button on failure
                    verificationCodeInput.focus();
                }
            })
            .catch(error => {
                console.error('Error verifying code:', error);
                statusSpan.textContent = 'Error verifying code. Check console.';
                statusSpan.className = 'verification-status error';
                verifyBtn.disabled = false; // Re-enable button on error
            });
        });

        // Optional: Prevent main form submission if email change is pending verification
        // mainForm.addEventListener('submit', function(event) {
        //     if (!emailVerified) {
        //         statusSpan.textContent = 'Please verify your new email address before saving changes.';
        //         statusSpan.className = 'verification-status error';
        //         event.preventDefault(); // Stop form submission
        //     }
        // });
    }
}); 