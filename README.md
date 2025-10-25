# Proyecto Recomienda

Esta es una aplicación de Android para recomendaciones de productos y servicios, desarrollada en Java siguiendo las especificaciones proporcionadas.

url de documentacion extensa de mi trayecto lo que hice en mi app: 
https://docs.google.com/document/d/1zZ-6stoWtuezg4N8a7BLnECt6WbHZHN8bEn7KLrZOCU/edit?usp=sharing

## Resumen Funcional

La aplicación permite a los usuarios:
- Iniciar sesión como invitado o simular un inicio con Google.
- Seleccionar sus categorías de interés en una pantalla de onboarding inicial.
- Ver una lista de productos obtenidos desde una API (`fakestoreapi.com`) y guardados localmente para uso sin conexión.
- Filtrar productos por categorías, incluyendo una sección personalizada "Para ti".
- Ver una sección destacada de "Ofertas del Día".
- Buscar productos por nombre o descripción.
- Marcar productos como "Me gusta" y verlos en una lista de favoritos.
- Ver los detalles completos de un producto.
- Añadir productos a un carrito de compras y ver el total.
- Cambiar sus preferencias de categorías o salir de la sesión desde la pantalla de configuración.
- Probar el sistema de notificaciones locales.
- La funcionalidad principal ha sido actualizada para permitir a los usuarios:
- Interactuar directamente con un asistente de IA a través de una interfaz de chat.
- Escribir peticiones de recomendaciones en lenguaje natural (ej: "recomiéndame una película de acción").
- Recibir respuestas generadas por la IA.
  ​
  *Nota: Las funcionalidades anteriores como el inicio de sesión, la selección de categorías, la lista de productos, el carrito de compras y los favoritos han sido reemplazadas por esta nueva interfaz de chat.*


## Requisitos Técnicos

- **Lenguaje**: Java
- **Arquitectura**: UI basada en Activities y Fragments con `BottomNavigationView`.
- **Base de Datos**: SQLite con `SQLiteOpenHelper`.
- **Red**: Retrofit y Gson para consumir la API REST.
- **UI**: AndroidX, Material Components, RecyclerView, ConstraintLayout.
- **Imágenes**: Glide para la carga y caché de imágenes.
- **Compilación**: Gradle con Kotlin DSL y Version Catalog.
- **Notificaciones**: Notificaciones locales de Android.
- **APIs Externas**:
- dummyjson para productos de prueba.
- webhook n8n para manejo de IA.
- Firebase Authentication para inicio de sesión con Google.
- groq api para consultas de IA.
- FOURSQUARE PLACES API para recomendaciones de lugares.
- OSMDROID para mapas y geolocalización.


## Cómo Compilar y Ejecutar

1.  Clona este repositorio.
2.  Abre el proyecto en la última versión estable de Android Studio.
3.  El proyecto utiliza `build.gradle.kts` y un catálogo de versiones (`libs.versions.toml`), por lo que Android Studio debería poder sincronizar y compilar el proyecto sin necesidad de configuración adicional.
4.  Ejecuta la aplicación en un emulador o en un dispositivo físico.

## Dependencias y APIs
- Retrofit para llamadas HTTP.
- Gson para parsing JSON.
- Glide para carga de imágenes.
- SQLite para almacenamiento local.
- Material Components para UI.
- Notificaciones locales de Android.
- dummyjson para productos de prueba.
- webhook n8n para manejo de IA.
- Firebase Authentication para inicio de sesión con Google.
- groq api para consultas de IA.
- FOURSQUARE PLACES API para recomendaciones de lugares.
- OSMDROID para mapas y geolocalización.


## Cómo Probar la Aplicación (Flujo de Prueba)

1.  **Inicio y Onboarding**:
    - Inicia la app. Serás recibido por la pantalla de Login.
    - Pulsa "Continuar como invitado".
    - o google Sign-In firebase
    - En la pantalla de Onboarding, selecciona algunas categorías de tu interés (ej. "electronics", "jewelery") y pulsa "Guardar y continuar".

2.  **Pantalla Principal (Home)**:
    - La app descargará los productos de la API y los mostrará. Verás una sección de "Ofertas" en la parte superior y la lista principal de productos abajo.
    - Usa el filtro de categorías en la parte superior para seleccionar "Para ti" y verás solo productos de las categorías que elegiste en el onboarding.
    - Pulsa un producto en la lista de "Ofertas" para ir a su detalle.

3.  **Interacción y Detalle**:
    - En la lista principal, pulsa el botón "Me gusta" en cualquier producto. Verás un mensaje de confirmación y el botón cambiará de estado.
    - Pulsa "Ver más" en otro producto. Serás llevado a la pantalla `ProductoDetalleActivity`.
    - En la pantalla de detalle, pulsa "Añadir al carrito".

4.  **Favoritos y Carrito**:
    - Vuelve a la pantalla principal y navega a la pestaña "Favoritos" usando la barra inferior. El producto que marcaste como "Me gusta" debería aparecer aquí.
    - En la barra superior de la app, pulsa el icono del carrito. Serás llevado a `CarritoActivity`, donde verás el producto que añadiste y el total.

5.  **Configuración**:
    - Ve a la pestaña "Configuración".
    - Pulsa "Probar Notificación" para recibir una notificación de ejemplo.
    - Pulsa "Salir" para borrar tus preferencias y volver a la pantalla de Login.
