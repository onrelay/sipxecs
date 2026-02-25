var searchPanel = {
    placeholder: null,

    register: function(searchBox, placeholder) {
        this.placeholder = placeholder;
        searchBox.value = this.placeholder;

        // Add initial class
        var container = document.getElementById('searchContainer');
        if (container) container.className = "styled";

        // Event handlers
        searchBox.addEventListener('focus', this.onClick.bind(this));
        searchBox.addEventListener('blur', this.onBlur.bind(this));
    },

    onClick: function(evt) {
        var sb = evt.target;
        if (sb.value === this.placeholder) {
            sb.value = "";
        }
        sb.className = "active";
    },

    onBlur: function(evt) {
        var sb = evt.target;
        if (sb.value === "") {
            sb.value = this.placeholder;
            sb.className = "inactive";
        }
    }
};