package com.wjb.bo;

import com.wjb.annotations.VlogIdValid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VlogBO {

    private String id;
    @NotBlank(message = "vlogerId不能为空")
    private String vlogerId;
    @NotBlank(message = "url不能为空")
    @Pattern(regexp = "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]",message = "url不正确")
    private String url;
    @NotBlank(message = "cover不能为空")
    private String cover;
    @NotBlank(message = "title不能为空")
    private String title;
    @NotNull(message = "width不能为空")
    @Digits(integer = 5, fraction = 0)
    private Integer width;
    @NotNull(message = "height不能为空")
    @Digits(integer = 5, fraction = 0)
    private Integer height;
    private Integer likeCounts;
    private Integer commentsCounts;
}