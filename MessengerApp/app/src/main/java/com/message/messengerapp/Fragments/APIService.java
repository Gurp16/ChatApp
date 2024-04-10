package com.message.messengerapp.Fragments;

import com.message.messengerapp.Notification.MyResponse;
import com.message.messengerapp.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAANDo2VuM:APA91bGrSlharJGIBShsBX4uTTybh2pdYnuk1p7v3JPz2MCndZgIBZfTEpLC76EK8pYiCBxveHnNFaBD0NA0AGO0b9qv8bKzzVyt5F36x2hz_VY82LOozY4AtCzbrx2jhhctWoJd60tl"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
