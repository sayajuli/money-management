const detailModal = document.getElementById('detailModal');
if (detailModal) {
  detailModal.addEventListener('show.bs.modal', event => {
    const button = event.relatedTarget;

    const type = button.getAttribute('data-type');
    const amount = button.getAttribute('data-amount');
    const category = button.getAttribute('data-category');
    const date = button.getAttribute('data-date');
    const description = button.getAttribute('data-description');

    const modalTitle = detailModal.querySelector('.modal-title');
    const detailType = detailModal.querySelector('#detailType');
    const detailAmount = detailModal.querySelector('#detailAmount');
    const detailCategory = detailModal.querySelector('#detailCategory');
    const detailDate = detailModal.querySelector('#detailDate');
    const detailDescription = detailModal.querySelector('#detailDescription');

    modalTitle.textContent = 'Detail Transaksi: ' + category;
    detailType.textContent = type;
    detailAmount.textContent = amount;
    detailCategory.textContent = category;
    detailDate.textContent = date;
    detailDescription.textContent = description;
  });
}