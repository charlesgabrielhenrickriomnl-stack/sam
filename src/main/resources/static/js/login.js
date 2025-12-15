const signUpButton = document.getElementById('signUp');
const signInButton = document.getElementById('signIn');
const container = document.getElementById('container');

signUpButton.addEventListener('click', () => {
    container.classList.add("right-panel-active");
});

signInButton.addEventListener('click', () => {
    container.classList.remove("right-panel-active");
});

// NEW LOGIC: Check for registration error passed from Thymeleaf via a global variable.
document.addEventListener('DOMContentLoaded', function() {
    // Check the variable set by the minimal inline Thymeleaf script block in login.html
    // This ensures that the page stays on the Sign Up tab when a registration submission fails.
    if (window.hasRegistrationError === true) {
        // Automatically switch to the Sign-Up panel
        container.classList.add("right-panel-active");
    }
});