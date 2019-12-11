# Strage [![API](https://img.shields.io/badge/API-21%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=21) [![](https://img.shields.io/badge/licence-MIT-blue.svg)]()

> Простой в использовании адаптер с поддержкой нескольких типов отображения

 
## Установка
Добавьте плагин **kotlin-kapt**, репозиторий **JitPack.io** и необходимые зависимости в файл *build.gradle* модуля приложения
```gradle
apply plugin: 'kotlin-kapt'
..
allprojects {
	repositories {
		maven { url 'https://jitpack.io' }
	}
}
..
dependencies {
	implementation 'com.github.nougust3:Strage:2.0'
	kapt 'com.github.nougust3:Strage-compiler:2.0'
}
```

## Использование
Выберите классы или примитивные типы для отображения
```kotlin
data class User(val age: String, val name: String)

val age = 26
val name = "Name"

val items = listOf(age, name, User(age, name))
```
Создайте класс адаптера с аннотацией **@Adapter**
```kotlin
@Adapter
class MainList {

	@BindId(R.layout.item_name)
	fun bind(data: String, title: TextView) {
		title.text = data
	}

	@BindName
	fun itenUser(data: String, name: TextView, ageEdit: EditText) {
		name.text = data.name
		ageEdit.setText(data.age.toString())
	}

}
```
Step 2. Create an instance of Strage and bind your models
```kotlin
@Adapter
class Main: BaseAdapter() {

    @BindName
    fun itemLayout(data: Model, titleView: TextView) {
        titleView.text = data.title
    }

    @Bind(R.layout.item2_layout)
    fun stringItem(data: Model2) {
        holder.view<TextView>(R.id.titleView).text = data.name
    }

}
recyclerView.adapter = adapter
```
