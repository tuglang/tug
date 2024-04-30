# tug
Is a programming language with a simple and neat syntax as well as minimal in terms of keywords and data types. Code execution speed is fast and minimal and the current version is `v0.1.0`.

## Syntax
**Reserved keywords:** `if`, `else`, `loop`, `skip`, `break`, `func`, `ret`

**Data types:** `str`, `num`, `none`, `func`, `table`

Hello, World program
```
print("Hello, World!")
```
Factorial using recursion
```
func factorial(n) {
  if n == 1 {
    ret n
  } else {
    ret n * factorial(n - 1)
  }
}
```

## To-do
- [x] Lexer
- [x] Parser
- [x] Interpreter
- [x] Variables
- [x] Functions
- [x] Data types
- [x] Statements
- [ ] Importing (Buggy)
- [ ] Libraries

## More examples
Cat program
```
loop {
  print(input())
}
```
FizzBuzz
```
n = 1
loop 20 {
  if n % 3 == 0 && n % 5 == 0 {
    print("FizzBuzz")
  } else if n % 3 == 0 {
    print("Fizz")
  } else if n % 5 == 0 {
    print("Buzz")
  } else {
    print(n}
  }
  n += 1
}
```
Table
```
account = {
  money = 100,
  bank = 0
}

func deposit(account, amount) {
  account.money -= amount
  account.bank += amount
}

func withdraw(account, amount) {
  account.money += amount
  account.bank -= amount
}

deposit(account, 50)
print(account.money)
```
