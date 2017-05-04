package com.fdu.xuqingqi.example.domain;

import com.library.common.database.DbColumn;
import com.library.common.database.DbTable;

/**
 * Description:
 * Author: xuqingqi
 * E-mail: xuqingqi01@gmail.com
 * Date: 2017/5/4
 */

@DbTable(table = "users")
public class User {

    @DbColumn(version = 1, column = "uid")
    public int id;

    @DbColumn(version = 1, column = "user_name")
    public String user_name;

    @DbColumn(version = 1, column = "nick_name")
    public String nick_name;

}
