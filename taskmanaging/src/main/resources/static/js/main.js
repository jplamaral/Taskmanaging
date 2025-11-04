document.addEventListener('DOMContentLoaded', function() {
    
    const btnToggleTarefa = document.getElementById('btn-toggle-tarefa');
    const formNovaTarefa = document.getElementById('form-nova-tarefa');
    
    const btnToggleRotina = document.getElementById('btn-toggle-rotina');
    const formNovaRotina = document.getElementById('form-nova-rotina');

    function toggleVisibility(element) {
        if (element.style.display === 'none' || element.style.display === '') {
            element.style.display = 'block';
        } else {
            element.style.display = 'none';
        }
    }

    if (btnToggleTarefa && formNovaTarefa) {
        btnToggleTarefa.addEventListener('click', function() {
            toggleVisibility(formNovaTarefa);
            if (formNovaRotina) formNovaRotina.style.display = 'none';
        });
    }

    if (btnToggleRotina && formNovaRotina) {
        btnToggleRotina.addEventListener('click', function() {
            toggleVisibility(formNovaRotina);
            if (formNovaTarefa) formNovaTarefa.style.display = 'none';
        });
    }
});