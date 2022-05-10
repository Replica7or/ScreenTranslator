package com.example.arknightstranslator;

import android.content.Intent;

public interface IOverlayService {
    void onActivityResult(int requestCode, int resultCode, Intent data);
}
