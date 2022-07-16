
import demo.Greeter

def call() {
   return  'Hola mundo desde script de mensaje'
}

def otherMessage() {
   return  "Other message call Greeter ${new Greeter().sayHello()}"
}