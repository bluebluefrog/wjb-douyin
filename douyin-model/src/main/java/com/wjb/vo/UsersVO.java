package com.wjb.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.Date;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UsersVO {

    private String id;
    private String mobile;
    private String nickname;
    private String imoocNum;
    private String face;
    private Integer sex;
    private Date birthday;
    private String country;
    private String province;
    private String city;
    private String district;
    private String description;
    private String bgImg;
    private Integer canImoocNumBeUpdated;
    private Date createdTime;
    private Date updatedTime;

    private String userToken;//用户token传递给前端

    private Integer myFollowsCounts;//my关注数量
    private Integer myFansCounts;//my粉丝数量
    //private Integer myLikeVlogCounts;//my点赞数量
    private Integer totalLikeMeCounts;//my获赞数量
}
