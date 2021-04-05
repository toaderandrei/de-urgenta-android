package ro.code4.deurgenta.helper

class Person(fName: String, personAge: Int) {

    var firstName = fName.capitalize()
    var age = personAge

    // initializer block
    init {
        firstName = fName.capitalize()
        println("First Name = $firstName")
        println("Age = $age")
    }
}