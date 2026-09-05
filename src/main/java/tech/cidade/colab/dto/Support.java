package tech.cidade.colab.dto;

import java.util.List;

public record Support(Integer count,
                      List<String> users) {
}
