# Strage

<img src="https://firebasestorage.googleapis.com/v0/b/home-page-852c4.appspot.com/o/strage.svg?alt=media&token=d7e463f3-a14e-407a-9b9e-fe66c7f65203" align="right" title="Strage logo" width="120" height="120">

> Strage is a simple adapter for RecyclerView with multiple view types.

 [![](https://jitpack.io/v/nougust3/Strage.svg)](https://jitpack.io/#nougust3/Strage)
 [![API](https://img.shields.io/badge/API-21%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=21)
 [![](https://img.shields.io/badge/licence-MIT-blue.svg)]()
 
## Gradle
Step 1. Add the JitPack repository to your build file 
```gradle
allprojects {
	repositories {
		maven { url 'https://jitpack.io' }
	}
}
```
Step 2. Add the dependency 
```gradle
dependencies {
	implementation 'com.github.nougust3:Strage:1.1.2'
}
```

## Usage:
Step 1. Create models that implement a common interface
```kotlin
data class Model1(
	var title: String
): ListItem

data class Model2(
	var name: String
): ListItem
```
```kotlin
val items = ArrayList<ListItem>()
```
Step 2. Create an instance of Strage and bind your models
```kotlin
val adapter = Strage(context, items)
	.bind<Model1>(R.layout.item1_layout) {
		view<TextView>(R.id.title).text = it.title
	}
	.bind<Model2>(R.layout.item2_layout) {
		view<TextView>(R.id.name).text = it.name
	}
	
recyclerView.adapter = adapter
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
