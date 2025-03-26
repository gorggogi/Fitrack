document.addEventListener("DOMContentLoaded", function () {
    const termsCheckbox = document.getElementById("termsCheckbox");
    const signupButton = document.getElementById("signupButton");


    termsCheckbox.addEventListener("change", function () {
        signupButton.disabled = !this.checked;
    });
});
