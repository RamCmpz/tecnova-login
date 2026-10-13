
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>TecNova | Iniciar sesión</title>

    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        :root {
            --fondo: #080d18;
            --panel: #101827;
            --azul: #4385ff;
            --celeste: #55d6ff;
            --texto: #f2f6ff;
            --secundario: #94a3b8;
            --borde: rgba(148, 163, 184, 0.18);
        }

        body {
            min-height: 100vh;
            font-family: "Segoe UI", Arial, sans-serif;
            color: var(--texto);
            background:
                radial-gradient(
                    ellipse at 15% 10%,
                    rgba(31, 83, 166, 0.30),
                    transparent 42%
                ),
                radial-gradient(
                    ellipse at 90% 90%,
                    rgba(0, 174, 255, 0.12),
                    transparent 38%
                ),
                linear-gradient(135deg, #080d18, #0d1628 55%, #080d18);

            display: flex;
            align-items: center;
            justify-content: center;
            padding: 28px 18px;
            overflow-x: hidden;
            position: relative;
        }

        /* Luces decorativas del fondo */
        body::before,
        body::after {
            content: "";
            position: fixed;
            width: 300px;
            height: 300px;
            border-radius: 50%;
            filter: blur(100px);
            opacity: 0.13;
            pointer-events: none;
        }

        body::before {
            background: #3478ff;
            top: -130px;
            right: -90px;
        }

        body::after {
            background: #00c8ff;
            bottom: -170px;
            left: -100px;
        }

        .contenedor {
            position: relative;
            z-index: 1;
            width: 100%;
            max-width: 440px;
        }

        .marca {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 13px;
            margin-bottom: 28px;
        }

        .logo {
            width: 53px;
            height: 53px;
            flex-shrink: 0;
            display: flex;
            align-items: center;
            justify-content: center;
            border: 1px solid rgba(85, 214, 255, 0.38);
            border-radius: 16px;
            background: linear-gradient(
                145deg,
                rgba(67, 133, 255, 0.22),
                rgba(16, 24, 39, 0.8)
            );
            box-shadow: 0 0 28px rgba(67, 133, 255, 0.12);
        }

        .logo svg {
            width: 34px;
            height: 34px;
        }

        .nombre {
            font-size: 29px;
            font-weight: 800;
            letter-spacing: -1px;
        }

        .nombre span {
            color: var(--celeste);
        }

        .marca small {
            display: block;
            color: var(--secundario);
            font-size: 10px;
            letter-spacing: 2.6px;
            margin-top: 3px;
        }

        .tarjeta {
            background: linear-gradient(
                145deg,
                rgba(20, 31, 49, 0.96),
                rgba(12, 19, 32, 0.98)
            );
            border: 1px solid var(--borde);
            border-radius: 22px;
            padding: 38px 34px 30px;
            box-shadow:
                0 24px 70px rgba(0, 0, 0, 0.36),
                inset 0 1px 0 rgba(255, 255, 255, 0.035);
        }

        .etiqueta {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            color: var(--celeste);
            background: rgba(67, 133, 255, 0.10);
            border: 1px solid rgba(67, 133, 255, 0.20);
            border-radius: 30px;
            padding: 7px 11px;
            font-size: 10px;
            font-weight: 700;
            letter-spacing: 1.2px;
            margin-bottom: 22px;
        }

        .punto {
            width: 6px;
            height: 6px;
            border-radius: 50%;
            background: var(--celeste);
            box-shadow: 0 0 10px var(--celeste);
        }

        h1 {
            font-size: clamp(27px, 5vw, 33px);
            line-height: 1.2;
            letter-spacing: -1px;
            margin-bottom: 12px;
        }

        .descripcion {
            color: var(--secundario);
            font-size: 14px;
            line-height: 1.7;
            margin-bottom: 30px;
        }

        .campo {
            margin-bottom: 21px;
        }

        label {
            display: block;
            font-size: 12px;
            font-weight: 600;
            color: #dce6f7;
            margin-bottom: 10px;
        }

        .entrada {
            display: flex;
            align-items: center;
            gap: 12px;
            border: 1px solid rgba(148, 163, 184, 0.22);
            background: rgba(5, 11, 21, 0.48);
            border-radius: 11px;
            padding: 0 14px;
            transition: border-color 0.2s, box-shadow 0.2s;
        }

        .entrada:focus-within {
            border-color: var(--azul);
            box-shadow: 0 0 0 3px rgba(67, 133, 255, 0.12);
        }

        .entrada svg {
            width: 19px;
            height: 19px;
            color: #8094b3;
            flex-shrink: 0;
        }

        input {
            width: 100%;
            min-width: 0;
            height: 49px;
            background: transparent;
            color: var(--texto);
            border: none;
            outline: none;
            font: inherit;
            font-size: 13px;
        }

        input::placeholder {
            color: #64748b;
        }

        .boton {
            width: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
            padding: 15px;
            margin-top: 9px;
            color: white;
            font-size: 13px;
            font-weight: 700;
            letter-spacing: 0.3px;
            border: 1px solid rgba(122, 177, 255, 0.45);
            border-radius: 11px;
            background: linear-gradient(100deg, #2865e8, #347ff5);
            box-shadow: 0 8px 24px rgba(37, 99, 235, 0.23);
            cursor: pointer;
            transition: transform 0.2s, filter 0.2s;
        }

        .boton:hover {
            filter: brightness(1.13);
            transform: translateY(-1px);
        }

        .boton:focus-visible {
            outline: 2px solid var(--celeste);
            outline-offset: 4px;
        }

        .boton svg {
            width: 17px;
            height: 17px;
        }

        .separador {
            height: 1px;
            background: var(--borde);
            margin: 27px 0 20px;
        }

        .seguridad {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            color: #8292aa;
            font-size: 11px;
            text-align: center;
        }

        .seguridad svg {
            width: 15px;
            height: 15px;
            color: #61b9a6;
            flex-shrink: 0;
        }

        footer {
            margin-top: 24px;
            text-align: center;
            color: #65738a;
            font-size: 11px;
            line-height: 1.8;
        }

        footer strong {
            color: #a9b8cd;
            font-weight: 600;
        }

        @media (max-width: 420px) {
            body {
                padding: 22px 14px;
            }

            .tarjeta {
                padding: 30px 22px 25px;
                border-radius: 18px;
            }

            .nombre {
                font-size: 26px;
            }

            .marca {
                margin-bottom: 22px;
            }
        }

        @media (prefers-reduced-motion: reduce) {
            *,
            *::before,
            *::after {
                transition: none !important;
            }
        }
    </style>
</head>

<body>

    <main class="contenedor">

        <header class="marca">

            <div class="logo">
                <!-- Logo tecnológico original en SVG -->
                <svg viewBox="0 0 40 40"
                     fill="none"
                     xmlns="http://www.w3.org/2000/svg"
                     aria-hidden="true">

                    <path d="M20 3L34 11V27L20 35L6 27V11L20 3Z"
                          stroke="#55D6FF"
                          stroke-width="1.8"/>

                    <path d="M12 14H28M20 14V27"
                          stroke="#FFFFFF"
                          stroke-width="2.5"
                          stroke-linecap="round"/>

                    <path d="M13 21H18"
                          stroke="#55D6FF"
                          stroke-width="2.5"
                          stroke-linecap="round"/>

                    <circle cx="28" cy="27" r="2.5" fill="#55D6FF"/>
                    <circle cx="12" cy="14" r="1.5" fill="#55D6FF"/>

                </svg>
            </div>

            <div>
                <div class="nombre">Tec<span>Nova</span></div>
                <small>TECNOLOGÍA E INNOVACIÓN</small>
            </div>

        </header>

        <section class="tarjeta">

            <div class="etiqueta">
                <span class="punto"></span>
                ACCESO AL SISTEMA
            </div>

            <h1>Bienvenido<br>de nuevo.</h1>

            <p class="descripcion">
                Ingresa tus credenciales para acceder al
                sistema de gestión de ventas e inventario.
            </p>

            <form action="#" method="post">

                <div class="campo">
                    <label for="usuario">Nombre de usuario</label>

                    <div class="entrada">
                        <svg viewBox="0 0 24 24"
                             fill="none"
                             stroke="currentColor"
                             stroke-width="1.7"
                             stroke-linecap="round"
                             stroke-linejoin="round"
                             aria-hidden="true">
                            <circle cx="12" cy="8" r="4"/>
                            <path d="M5 21V19C5 15.7 8.1 13 12 13C15.9 13 19 15.7 19 19V21"/>
                        </svg>

                        <input
                            type="text"
                            id="usuario"
                            name="usuario"
                            placeholder="Ingresa tu usuario"
                            autocomplete="username"
                            required
                        >
                    </div>
                </div>

                <div class="campo">
                    <label for="contrasena">Contraseña</label>

                    <div class="entrada">
                        <svg viewBox="0 0 24 24"
                             fill="none"
                             stroke="currentColor"
                             stroke-width="1.7"
                             stroke-linecap="round"
                             stroke-linejoin="round"
                             aria-hidden="true">
                            <rect x="4" y="10" width="16" height="11" rx="2"/>
                            <path d="M8 10V7A4 4 0 0 1 16 7V10"/>
                            <circle cx="12" cy="15" r="1"/>
                            <path d="M12 16V18"/>
                        </svg>

                        <input
                            type="password"
                            id="contrasena"
                            name="contrasena"
                            placeholder="Ingresa tu contraseña"
                            autocomplete="current-password"
                            required
                        >
                    </div>
                </div>

                <button class="boton" type="submit">
                    Iniciar sesión

                    <svg viewBox="0 0 24 24"
                         fill="none"
                         stroke="currentColor"
                         stroke-width="2"
                         stroke-linecap="round"
                         stroke-linejoin="round"
                         aria-hidden="true">
                        <path d="M5 12H19"/>
                        <path d="M12 5L19 12L12 19"/>
                    </svg>
                </button>

            </form>

            <div class="separador"></div>

            <div class="seguridad">
                <svg viewBox="0 0 24 24"
                     fill="none"
                     stroke="currentColor"
                     stroke-width="1.8"
                     stroke-linecap="round"
                     stroke-linejoin="round"
                     aria-hidden="true">
                    <path d="M12 22S20 18 20 11V5L12 2L4 5V11C4 18 12 22 12 22Z"/>
                    <path d="M9 12L11 14L15 10"/>
                </svg>

                Sistema de gestión TecNova
            </div>

        </section>

        <footer>
            <strong>TecNova</strong> · Sistema de ventas e inventario<br>
            © 2026 Todos los derechos reservados
        </footer>

    </main>

</body>
</html>
