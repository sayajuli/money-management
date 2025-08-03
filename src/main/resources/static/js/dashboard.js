document.addEventListener("DOMContentLoaded", function () {
  fetch('/api/expense-summary')
    .then(response => response.json())
    .then(data => {
      const ctx = document.getElementById('myPieChart').getContext('2d');
      const labels = Object.keys(data);
      const values = Object.values(data);

      if (labels.length === 0) {
        ctx.font = "16px sans-serif";
        ctx.textAlign = "center";
        ctx.fillText("Belum ada data pengeluaran untuk ditampilkan.", ctx.canvas.width / 2, ctx.canvas.height / 2);
        return;
      }

      new Chart(ctx, {
        type: 'pie',
        data: {
          labels: labels,
          datasets: [{
            data: values,
            backgroundColor: [
              '#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b',
              '#858796', '#5a5c69', '#fd7e14', '#6f42c1', '#e83e8c'
            ],
            hoverOffset: 4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: 'top',
            },
            title: {
              display: false,
              text: 'Pengeluaran per Kategori'
            }
          }
        }
      });
    })
    .catch(error => {
      console.error('Gagal mengambil data untuk grafik:', error);
      const ctx = document.getElementById('myPieChart').getContext('2d');
      ctx.font = "16px sans-serif";
      ctx.textAlign = "center";
      ctx.fillText("Gagal memuat data grafik.", ctx.canvas.width / 2, ctx.canvas.height / 2);
    });
});