document.addEventListener('DOMContentLoaded', function() {
    const ctx = document.getElementById('bodyCompositionChart').getContext('2d');
    const measurements = JSON.parse(document.getElementById('measurementsData').textContent);
    
    const dates = measurements.map(m => new Date(m.date).toLocaleDateString());
    const weights = measurements.map(m => m.weight);
    const bodyFatPercentages = measurements.map(m => m.bodyFatPercentage);
    const bmi = measurements.map(m => {
        const heightInMeters = m.user.height / 100;
        return m.weight / (heightInMeters * heightInMeters);
    });
    
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: dates,
            datasets: [
                {
                    label: 'Weight (kg)',
                    data: weights,
                    borderColor: 'rgba(75, 192, 192, 1)',
                    yAxisID: 'y'
                },
                {
                    label: 'Body Fat %',
                    data: bodyFatPercentages,
                    borderColor: 'rgba(153, 102, 255, 1)',
                    yAxisID: 'y1'
                },
                {
                    label: 'BMI',
                    data: bmi,
                    borderColor: 'rgba(255, 159, 64, 1)',
                    yAxisID: 'y2'
                }
            ]
        },
        options: {
            responsive: true,
            interaction: {
                mode: 'index',
                intersect: false,
            },
            scales: {
                y: {
                    type: 'linear',
                    display: true,
                    position: 'left',
                    title: {
                        display: true,
                        text: 'Weight (kg)'
                    }
                },
                y1: {
                    type: 'linear',
                    display: true,
                    position: 'right',
                    title: {
                        display: true,
                        text: 'Body Fat %'
                    },
                    grid: {
                        drawOnChartArea: false
                    }
                },
                y2: {
                    type: 'linear',
                    display: true,
                    position: 'right',
                    title: {
                        display: true,
                        text: 'BMI'
                    },
                    grid: {
                        drawOnChartArea: false
                    }
                }
            }
        }
    });
}); 