# Strage

Strage is a simple adapter for RecyclerView with multiple view types.

## Usage:
```kotlin
  interface IModel { }

  data class Model1(var title: String): IModel

  val items = ArrayList<IModel>()

  val adapter = Strage(this, items)
          .bind<Model1>(R.layout.item1_layout) {
              title.text = it.title
          }
          .bind<Model2>(R.layout.item2_layout) {
              date.text = it.date
          }
          .bind<Model3>(R.layout.item3_layout) {
              btn.onClickListener { }
          }
```

```groovy
	allprojects {
		repositories {
			maven { url 'https://jitpack.io' }
		}
	}

	dependencies {
	  implementation 'com.github.nougust3:Strage:1.0'
	}
```
