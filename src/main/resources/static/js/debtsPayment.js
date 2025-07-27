const paymentModal = document.getElementById('paymentModal');
paymentModal.addEventListener('show.bs.modal', event => {
  const button = event.relatedTarget;

  const debtId = button.getAttribute('data-debt-id');
  const debtName = button.getAttribute('data-debt-name');
  const debtRemaining = parseFloat(button.getAttribute('data-debt-remaining')).toLocaleString('id-ID', { style: 'currency', currency: 'IDR' });

  const modalDebtIdInput = paymentModal.querySelector('#modalDebtId');
  const modalDebtNameElement = paymentModal.querySelector('#modalDebtName');
  const modalRemainingAmountElement = paymentModal.querySelector('#modalRemainingAmount');
  const modalAmountInput = paymentModal.querySelector('#modalAmount');

  modalDebtIdInput.value = debtId;
  modalDebtNameElement.textContent = debtName;
  modalRemainingAmountElement.textContent = debtRemaining;
  modalAmountInput.value = '';
});