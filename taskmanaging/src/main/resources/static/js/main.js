// Aguarda o DOM (estrutura da página) estar totalmente carregado
document.addEventListener('DOMContentLoaded', () => {

    // Lógica do seletor de tema (Dark/Light Mode)
    // ======================================================
    
    // Seleciona todos os botões de tema (pode ter um no navbar, outro no auth)
    const themeToggleButtons = document.querySelectorAll('#theme-toggle-btn, .theme-toggle-auth');
    
    // Ícones (assumindo que existem em algum lugar da página se o botão existir)
    const iconDark = document.getElementById('theme-toggle-icon-dark');
    const iconLight = document.getElementById('theme-toggle-icon-light');

    // Função para atualizar o ícone
    const updateIcon = (isDarkMode) => {
        if (iconDark && iconLight) {
            iconDark.style.display = isDarkMode ? 'inline-block' : 'none';
            iconLight.style.display = isDarkMode ? 'none' : 'inline-block';
        }
    };

    // Função para alternar o tema
    const toggleTheme = () => {
        // Verifica se a classe .dark-mode está no <html>
        let isDarkMode = document.documentElement.classList.toggle('dark-mode');
        
        // Salva a preferência no localStorage
        localStorage.setItem('theme', isDarkMode ? 'dark' : 'light');
        
        // Atualiza o ícone
        updateIcon(isDarkMode);
    };

    // Adiciona o evento de clique a todos os botões de tema
    themeToggleButtons.forEach(btn => {
        btn.addEventListener('click', toggleTheme);
    });

    // Atualiza o ícone na carga inicial da página
    let currentTheme = localStorage.getItem('theme') === 'dark';
    updateIcon(currentTheme);


    // 1. Lógica para a Página de Autenticação (auth.html)
    // ======================================================
    // (Este código você já tinha, mantenha ele)
    
    const btnLogin = document.getElementById('btn-login');
    const btnCadastro = document.getElementById('btn-cadastro');
    const formLogin = document.getElementById('form-login');
    const formCadastro = document.getElementById('form-cadastro');

    if (btnLogin && btnCadastro && formLogin && formCadastro) {
        
        function showLogin() {
            formLogin.style.display = 'block';
            formCadastro.style.display = 'none';
            btnLogin.classList.add('active-tab');
            btnCadastro.classList.remove('active-tab');
        }

        function showCadastroForm() {
            formLogin.style.display = 'none';
            formCadastro.style.display = 'block';
            btnCadastro.classList.add('active-tab');
            btnLogin.classList.remove('active-tab');
        }

        btnLogin.addEventListener('click', showLogin);
        btnCadastro.addEventListener('click', showCadastroForm);

        const showCadastroOnLoad = document.body.dataset.showCadastro === 'true';
        if (showCadastroOnLoad) {
            showCadastroForm();
        }

        const senhaInput = document.getElementById('senha');
        const confirmarSenhaInput = document.getElementById('confirmarSenha');
        const erroSenha = document.getElementById('erroSenha');
        const formCadastroEl = document.getElementById('form-cadastro');

        if (formCadastroEl) {
            formCadastroEl.addEventListener('submit', function (event) {
                if (senhaInput.value !== confirmarSenhaInput.value) {
                    event.preventDefault(); 
                    erroSenha.style.display = 'block';
                    confirmarSenhaInput.classList.add('is-invalid');
                } else {
                    erroSenha.style.display = 'none';
                    confirmarSenhaInput.classList.remove('is-invalid');
                }
            });
        }
    }

    // 2. Lógica para "Mostrar/Ocultar Senha"
    // ========================================================================
    // (Este código você já tinha, mantenha ele)
    document.querySelectorAll('.toggle-password').forEach(icon => {
        icon.addEventListener('click', () => {
            const targetId = icon.getAttribute('data-target');
            const input = document.getElementById(targetId);
            
            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.remove('bi-eye');
                icon.classList.add('bi-eye-slash');
            } else {
                input.type = 'password';
                icon.classList.remove('bi-eye-slash');
                icon.classList.add('bi-eye');
            }
        });
    });


    // 3. Lógica para o Dashboard (index.html)
    // ======================================================
    // (Este código você já tinha, mantenha ele)
    const btnToggleTarefa = document.getElementById('btn-toggle-tarefa');
    const btnToggleRotina = document.getElementById('btn-toggle-rotina');
    const formNovaTarefa = document.getElementById('form-nova-tarefa');
    const formNovaRotina = document.getElementById('form-nova-rotina');

    if (btnToggleTarefa && btnToggleRotina && formNovaTarefa && formNovaRotina) {
        
        btnToggleTarefa.addEventListener('click', () => {
            formNovaTarefa.style.display = 'block';
            formNovaRotina.style.display = 'none';
            btnToggleTarefa.classList.add('btn-primary');
            btnToggleTarefa.classList.remove('btn-outline-secondary');
            btnToggleRotina.classList.add('btn-outline-secondary');
            btnToggleRotina.classList.remove('btn-primary');
        });

        btnToggleRotina.addEventListener('click', () => {
            formNovaTarefa.style.display = 'none';
            formNovaRotina.style.display = 'block';
            btnToggleRotina.classList.add('btn-primary');
            btnToggleRotina.classList.remove('btn-outline-secondary');
            btnToggleTarefa.classList.add('btn-outline-secondary');
            btnToggleTarefa.classList.remove('btn-primary');
        });
    }

});