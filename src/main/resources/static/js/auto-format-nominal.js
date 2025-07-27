function formatNumber(n) {
  return n.toString().replace(/\D/g, "").replace(/\B(?=(\d{3})+(?!\d))/g, ".");
}

function setupNominalInputs() {
  const nominalInputs = document.querySelectorAll('.nominal-input');

  nominalInputs.forEach(input => {
    if(input.value) {
      input.value = formatNumber(input.value);
    }

    input.addEventListener('input', (e) => {
      e.target.value = formatNumber(e.target.value);
    });

    const parentForm = input.closest('form');
    if(parentForm) {
      parentForm.addEventListener('submit', () => {
        input.value = input.value.replace(/\./g, '');
      });
    }
  });
}