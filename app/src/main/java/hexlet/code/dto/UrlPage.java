package hexlet.code.dto;

import hexlet.code.model.UrlCheck;
import hexlet.code.model.Url;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UrlPage extends BasePage {
    private Url url;
    private List<UrlCheck> checks;
}
