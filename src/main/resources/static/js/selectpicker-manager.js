(function() {
    'use strict';

    // Track initialized selects globally
    const initializedSelects = new Set();
    let isInitializing = false;

    // Wait for jQuery to be available
    function waitForJQuery(callback, maxAttempts) {
        maxAttempts = maxAttempts || 20;
        let attempts = 0;

        function check() {
            attempts++;
            if (typeof $ !== 'undefined' && $.fn && $.fn.selectpicker) {
                callback();
            } else if (attempts < maxAttempts) {
                setTimeout(check, 100);
            } else {
                console.error('jQuery or Bootstrap Select not available after ' + maxAttempts + ' attempts');
            }
        }

        check();
    }

    // Function to properly initialize a selectpicker
    window.initializeSelectPicker = function(element) {
        try {
            if (typeof $ === 'undefined' || !$.fn.selectpicker) {
                console.warn('Bootstrap Select not available');
                return;
            }

            const $select = $(element);

            // Skip if element is not a select
            if (!$select.is('select')) {
                console.warn('Element is not a select:', element);
                return;
            }

            // Generate a unique identifier
            const selectId = $select.attr('id') || 'select-' + Math.random().toString(36).substr(2, 9);
            if (!$select.attr('id')) {
                $select.attr('id', selectId);
            }

            // Check if already initialized
            if (initializedSelects.has(selectId)) {
                // Just refresh if needed
                if ($select.data('selectpicker')) {
                    $select.selectpicker('refresh');
                    $select.selectpicker('render');
                }
                return;
            }

            // Check for existing wrapper
            const wrapper = $select.closest('.bootstrap-select');
            if (wrapper.length) {
                // Destroy existing instance
                if ($select.data('selectpicker')) {
                    $select.selectpicker('destroy');
                }
                // Remove wrapper
                wrapper.replaceWith($select);
                // Recursive call after DOM update
                setTimeout(function() {
                    window.initializeSelectPicker($select[0]);
                }, 50);
                return;
            }

            // Destroy any lingering instance
            if ($select.data('selectpicker')) {
                $select.selectpicker('destroy');
            }

            // Initialize fresh
            $select.selectpicker({
                iconBase: 'bi',
                tickIcon: 'bi-check',
                liveSearch: false,
                size: 5,
                style: 'btn-sm btn-outline-secondary'
            });

            // Mark as initialized
            initializedSelects.add(selectId);
            console.log('Initialized select:', selectId);
        } catch (error) {
            console.error('Error initializing selectpicker:', error);
        }
    };

    // Function to initialize all selectpickers in a container
    window.initializeAllSelectPickers = function(container) {
        if (isInitializing) return;

        isInitializing = true;
        const target = container || document;

        setTimeout(function() {
            try {
                if (typeof $ === 'undefined' || !$.fn.selectpicker) {
                    console.warn('jQuery or Bootstrap Select not available');
                    isInitializing = false;
                    return;
                }

                $(target).find('select.selectpicker').each(function() {
                    const $select = $(this);

                    // Skip if select is hidden or has no options
                    if ($select.is(':hidden') || $select.find('option').length === 0) {
                        return;
                    }

                    const selectId = $select.attr('id') || 'select-' + Math.random().toString(36).substr(2, 9);

                    // If already initialized, just refresh
                    if (initializedSelects.has(selectId) && $select.data('selectpicker')) {
                        $select.selectpicker('refresh');
                        $select.selectpicker('render');
                    } else {
                        window.initializeSelectPicker(this);
                    }
                });
            } catch (error) {
                console.error('Error initializing selectpickers:', error);
            } finally {
                isInitializing = false;
            }
        }, 150);
    };

    // Handle HTMX events
    document.addEventListener('htmx:afterSwap', function(event) {
        try {
            if (event.detail.target) {
                setTimeout(function() {
                    window.initializeAllSelectPickers(event.detail.target);
                }, 200);
            }
        } catch (error) {
            console.error('Error in htmx:afterSwap:', error);
        }
    });

    document.addEventListener('htmx:afterSettle', function(event) {
        try {
            if (event.detail.target) {
                setTimeout(function() {
                    window.initializeAllSelectPickers(event.detail.target);
                }, 100);
            }
        } catch (error) {
            console.error('Error in htmx:afterSettle:', error);
        }
    });

    // Handle form submission that replaces entire container
    document.addEventListener('htmx:afterRequest', function(event) {
        try {
            if (event.detail.target && event.detail.target.id === 'tasks-container') {
                setTimeout(function() {
                    window.initializeAllSelectPickers(event.detail.target);
                }, 300);
            }
        } catch (error) {
            console.error('Error in htmx:afterRequest:', error);
        }
    });

    // Initialize on page load - with retry
    function initializeOnLoad() {
        try {
            if (typeof $ !== 'undefined' && $.fn.selectpicker) {
                window.initializeAllSelectPickers(document);
            } else {
                console.warn('jQuery or Bootstrap Select not loaded yet, retrying...');
                setTimeout(initializeOnLoad, 500);
            }
        } catch (error) {
            console.error('Error in initializeOnLoad:', error);
            // Retry after a delay
            setTimeout(initializeOnLoad, 1000);
        }
    }

    // Start initialization when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            initializeOnLoad();
        });
    } else {
        initializeOnLoad();
    }

    console.log('SelectPicker Manager loaded successfully');
})();