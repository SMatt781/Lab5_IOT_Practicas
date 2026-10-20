package com.example.lab5_practica1.common;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.*;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.*;
import com.google.android.material.appbar.MaterialToolbar;

public abstract class BaseActivity extends AppCompatActivity {
  private final ActivityResultLauncher<String> permission =
      registerForActivityResult(
          new ActivityResultContracts.RequestPermission(),
          granted -> {
            Toast.makeText(
                    this,
                    granted
                        ? "Notificaciones permitidas"
                        : "Los datos se guardan aunque no permitas avisos",
                    Toast.LENGTH_LONG)
                .show();
            onNotificationPermissionChanged();
          });

  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    EdgeToEdge.enable(this);
    // El tema de estas prácticas es claro, incluso si el teléfono está en modo oscuro.
    WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
        .setAppearanceLightStatusBars(true);
    WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
        .setAppearanceLightNavigationBars(true);
  }

  protected void showContent(View root) {
    setContentView(root);
    final int left = root.getPaddingLeft(),
        top = root.getPaddingTop(),
        right = root.getPaddingRight(),
        bottom = root.getPaddingBottom();
    ViewCompat.setOnApplyWindowInsetsListener(
        root,
        (view, insets) -> {
          Insets system =
              insets.getInsets(
                  WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
          Insets keyboard = insets.getInsets(WindowInsetsCompat.Type.ime());
          view.setPadding(
              left + system.left,
              top + system.top,
              right + system.right,
              bottom + Math.max(system.bottom, keyboard.bottom));
          return insets;
        });
    ViewCompat.requestApplyInsets(root);
  }

  protected void backToolbar(MaterialToolbar toolbar) {
    toolbar.setNavigationIcon(com.example.lab5_practica1.R.drawable.ic_back);
    toolbar.setNavigationOnClickListener(v -> finish());
  }

  protected void askNotifications() {
    if (Build.VERSION.SDK_INT >= 33
        && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED)
      permission.launch(Manifest.permission.POST_NOTIFICATIONS);
    else
      Toast.makeText(
              this,
              "Permiso concedido. Revisa también los canales en Ajustes si no ves avisos.",
              Toast.LENGTH_LONG)
          .show();
  }

  protected void onNotificationPermissionChanged() {}

  protected void message(String text) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show();
  }
}
