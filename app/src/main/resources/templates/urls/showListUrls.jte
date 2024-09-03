@import hexlet.code.dto.UrlsPage
@import hexlet.code.util.NamedRoutes
@import java.time.format.DateTimeFormatter
@param UrlsPage page
@param String formatter = "dd/MM/yyyy HH:mm"

@template.layout.page(
page=page,
content = @`
    <div class="container-lg mt-5">
        <h1>Сайты</h1>
        <table class="table table-bordered table-hover mt-3">
            <thead>
            <tr>
                <th class="col-1">id</th>
                <th>Имя</th>
                <th class="col-2">Последняя проверка</th>
                <th class="col-1">Код ответа</th>
            </tr>
            </thead>
            <tbody>
            @if(!page.getUrls().isEmpty())
                @for(var url : page.getUrls())
                    <tr>
                        <td>${url.getId()}</td>
                        <td><a href="${NamedRoutes.urlPath(url.getId())}">${url.getName()} </a></td>
                        @if(page.getChecks().get(url.getId())==null)
                            <td></td>
                            <td></td>
                        @elseif(page.getChecks().get(url.getId())!=null)
                            <td>
                                ${page.getChecks().get(url.getId()).getCreatedAt().toLocalDateTime().format(DateTimeFormatter.ofPattern(formatter))}
                            </td>
                            <td>
                                ${page.getChecks().get(url.getId()).getStatusCode()}
                            </td>
                        @endif
                    </tr>
                @endfor
            @endif
            </tbody>
        </table>
    </div>
`
)