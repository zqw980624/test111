package com.yami.trading.bean.user.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChildrenLever {

    private List<String> lever1 = new ArrayList<String>();
    private List<String> lever2 = new ArrayList<String>();
    private List<String> lever3 = new ArrayList<String>();
    private List<String> lever4 = new ArrayList<String>();
}
