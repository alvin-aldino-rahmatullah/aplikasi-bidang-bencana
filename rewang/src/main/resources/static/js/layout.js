document.addEventListener('DOMContentLoaded', function () {
    var layout = document.querySelector('.layout');
    var toggle = document.getElementById('sidebarToggle');
    if (!layout || !toggle) return;

    if (window.innerWidth < 992) {
        layout.classList.add('sidebar-hidden');
    }

    toggle.addEventListener('click', function () {
        layout.classList.toggle('sidebar-hidden');
    });
});