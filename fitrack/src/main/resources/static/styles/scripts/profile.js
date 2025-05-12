document.addEventListener('DOMContentLoaded', function() {
    // Function to preview the profile image before uploading
    function previewImage(input) {
        if (input.files && input.files[0]) {
            const reader = new FileReader();
            reader.onload = function(e) {
                const preview = document.querySelector('.profile-picture-preview');
                if (preview) {
                    preview.src = e.target.result;
                } else {
                    console.error("Profile picture preview element not found.");
                }
            };
            reader.readAsDataURL(input.files[0]);
        }
    }

    // Add event listener to the file input if it exists
    const profilePictureInput = document.getElementById('profilePicture');
    if (profilePictureInput) {
        profilePictureInput.addEventListener('change', function() {
            previewImage(this); // Call the preview function when the file changes
        });
    } else {
        // This might happen if the script loads before the element, although DOMContentLoaded should prevent this.
        // Or if the ID is different.
        console.warn("Profile picture input element not found on DOM load.");
    }
});
