package com.client.util;

import com.db.Chat;
import com.db.SignedUser;

public class ReceiverPhone {
    public static String get(Chat chat) {

        String receiverPhone =chat.getUser1();

        if(receiverPhone.equals(SignedUser.phone))
        {
            receiverPhone = chat.getUser2();
        }
        return receiverPhone;
    }
}
