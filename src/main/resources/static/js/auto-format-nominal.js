function formatNumber(n) {
    const cleaned = n.toString().replace(/\D/g, '');
    return cleaned.replace(/\B(?=(\d{3})+(?!\d))/g, ".");
}

function setupNominalInputs() {
    const nominalInputs = document.querySelectorAll('.nominal-input');

    nominalInputs.forEach(input => {
        if (input.value) {
            input.value = formatNumber(input.value);
        }
        input.removeEventListener('input', handleInputFormatting);
        input.addEventListener('input', handleInputFormatting);

        const parentForm = input.closest('form');
        if (parentForm) {
            parentForm.removeEventListener('submit', handleFormSubmit);
            parentForm.addEventListener('submit', () => handleFormSubmit(input));
        }
    });
}

function handleInputFormatting(e) {
    const originalCursorPosition = e.target.selectionStart;
    const originalLength = e.target.value.length;

    const formattedValue = formatNumber(e.target.value);
    e.target.value = formattedValue;

    const newLength = formattedValue.length;
    const newCursorPosition = originalCursorPosition + (newLength - originalLength);
    e.target.selectionStart = e.target.selectionEnd = newCursorPosition;
}

function handleFormSubmit(inputElement) {
    if (inputElement && inputElement.value) {
        const unformattedValue = inputElement.value.replace(/\./g, '');
        inputElement.value = unformattedValue;
    }
}

window.addEventListener('load', setupNominalInputs);