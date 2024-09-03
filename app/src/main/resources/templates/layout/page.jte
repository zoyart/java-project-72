@import gg.jte.Content
@import hexlet.code.dto.BasePage
@import hexlet.code.util.NamedRoutes
@param BasePage  page =null
@param Content content

<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Анализатор страниц</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
            crossorigin="anonymous"></script>
</head>

<body class="d-flex flex-column min-vh-100">
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container-fluid">
        <a class="navbar-brand" href="${NamedRoutes.rootPath()}">Анализатор страниц</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav"
                aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <div class="navbar-nav">
                <a class="nav-link" href="${NamedRoutes.rootPath()}">Главная</a>
                <a class="nav-link" href="${NamedRoutes.listUrlsPath()}">Сайты</a>
            </div>
        </div>
    </div>
</nav>

<main class="flex-grow-1">

    @if(page!=null && page.getFlash()!=null)
        @if(page.getFlash().equals("wrong"))
            <div class="rounded-0 m-0 alert alert-dismissible fade show alert-danger" role="alert">
                ${page.getFlashType()}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>

        @elseif(page.getFlash().equals("success"))
            <div class="rounded-0 m-0 alert alert-dismissible fade show alert-success" role="alert">
                ${page.getFlashType()}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        @elseif(page.getFlash().equals("unchanged"))
            <div class="rounded-0 m-0 alert alert-dismissible fade show alert-info" role="alert">
                ${page.getFlashType()}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        @elseif(page.getFlash().equals("danger"))
            <div class="rounded-0 m-0 alert alert-dismissible fade show alert-danger" role="alert">
                ${page.getFlashType()}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        @endif
    @endif

    <section>
        ${content}
    </section>
</main>

<footer class="footer border-top py-3 mt-5 bg-light">
    <div class="container-xl">
        <div class="text-center">
            created by <a href="https://ru.hexlet.io" target="_blank">Hexlet</a>
        </div>
    </div>
</footer>

</body>
</html>
