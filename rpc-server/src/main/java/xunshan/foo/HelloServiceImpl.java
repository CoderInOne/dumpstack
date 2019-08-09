package xunshan.foo;

public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        System.out.println("Calling HelloService#sayHello(" + name + ")");

        return "Hello, " + name + "!";
    }
}
