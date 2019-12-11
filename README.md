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
**1.** Выберите классы или примитивные типы для отображения
```kotlin
data class User(val age: String, val name: String)

val age = 26
val name = "Name"

val items = listOf(age, name, User(age, name))
```
**2.** Создайте класс адаптера с аннотацией **@Adapter** и методы с аннотациями для каждого типа отображения
> Аннотация **@BindId**. Принимает в качестве аргумента идентификатор файла разметки
> Аннотация **@BindName**. Указывает на использование имени метода в качесве указателя на файл разметки
```kotlin
@Adapter
class MainList {

	@BindId(R.layout.item_name)
	fun bind(name: String, title: TextView) {
		title.text = name
	}

	@BindName
	fun itenUser(user: User, name: TextView, ageEdit: EditText) {
		name.text = user.name
		ageEdit.setText(user.age.toString())
	}

}
```
**3.** Примените сгенерированный адаптер к списку и зайдайте значения
```kotlin
val adapter = MainListAdapter().get()
recyclerView.adapter = adapter
adapter.setItems(items)
```
