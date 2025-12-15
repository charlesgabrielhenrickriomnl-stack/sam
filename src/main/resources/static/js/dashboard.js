function previewImage(event) {
    const reader = new FileReader();
    const imageField = document.getElementById('avatarPreview');
    const uploadButton = document.getElementById('uploadBtn');

    reader.onload = function() {
        if (reader.readyState === 2) {
            imageField.src = reader.result;
        }
    }

    if (event.target.files[0]) {
        reader.readAsDataURL(event.target.files[0]);
        uploadButton.classList.add('active');
    } else {
        uploadButton.classList.remove('active');
    }
}

document.addEventListener('DOMContentLoaded', function() {
    // --- Profile Dropdown Logic ---
    const dropdownBtn = document.getElementById('profileDropdownBtn');
    const dropdownMenu = document.getElementById('profileDropdownMenu');
    const dropdownContainer = document.querySelector('.profile-dropdown');

    if (dropdownBtn) {
        dropdownBtn.addEventListener('click', function(event) {
            event.stopPropagation();
            dropdownMenu.classList.toggle('show');
            dropdownContainer.classList.toggle('active');
        });
    }

    window.addEventListener('click', function(event) {
        if (dropdownMenu && dropdownMenu.classList.contains('show')) {
            if (!dropdownBtn.contains(event.target)) {
                dropdownMenu.classList.remove('show');
                dropdownContainer.classList.remove('active');
            }
        }
    });

    // --- Settings Page Panel Toggling ---
    const navLinks = document.querySelectorAll('.settings-nav-link');
    const panels = document.querySelectorAll('.settings-panel');
    
    // Function to set the active tab based on URL hash
    function setActiveTab() {
        // Default to #account if no hash is present
        const hash = window.location.hash || '#account';
        
        let activeLink = document.querySelector(`.settings-nav-link[href="${hash}"]`);
        let activePanel = document.getElementById(hash.substring(1));

        // Deactivate all first
        navLinks.forEach(l => l.classList.remove('active'));
        panels.forEach(p => p.classList.remove('active'));

        // Activate the correct ones
        if (activeLink) {
            activeLink.classList.add('active');
        }
        if (activePanel) {
            activePanel.classList.add('active');
        }
    }

    // Set active tab on initial page load
    setActiveTab();

    navLinks.forEach(link => {
        link.addEventListener('click', function(event) {
            event.preventDefault();
            const newHash = this.getAttribute('href');
            
            // Update URL without reloading the page
            if (history.pushState) {
                history.pushState(null, null, newHash);
            } else {
                window.location.hash = newHash;
            }
            
            setActiveTab();
        });
    });

    // --- Auto-dismiss alert messages ---
    const alertMessages = document.querySelectorAll('.settings-content .message, .settings-content .error');

    alertMessages.forEach(function(message) {
        setTimeout(() => {
            message.style.transition = 'opacity 0.5s ease';
            message.style.opacity = '0';
            setTimeout(() => {
                message.style.display = 'none';
            }, 500); // Wait for fade out to complete
        }, 1300); // 1.3 seconds
    });
});