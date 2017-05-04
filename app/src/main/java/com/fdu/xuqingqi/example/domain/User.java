package com.fdu.xuqingqi.example.domain;

import com.library.common.database.DatabaseColumn;
import com.library.common.database.DatabaseTable;

/**
 * Description:
 * Author: xuqingqi
 * E-mail: xuqingqi01@gmail.com
 * Date: 2017/5/4
 */

@DatabaseTable(table = "users")
public class User {

    @DatabaseColumn(version = 1, column = "uid")
    public int id;

    @DatabaseColumn(version = 1, column = "user_name")
    public String user_name;

    @DatabaseColumn(version = 1, column = "nick_name")
    public String nick_name;

}
