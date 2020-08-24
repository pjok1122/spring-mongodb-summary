MongoTemplate은 `find`, `findAndModify`, `findAndReplace`, `findOne`, `insert`, `remove`, `save`, `update`, `updateMulti` 등 편리한 기능들을 제공함.
MongoDB driver 대신에 MongoOperations을 사용하는 구조인데, `Document` 타입 대신에 Java Object를 사용함으로써 더욱 다루기 쉬워지고, 다양한 API를 사용할 수 있음. (Query, Criteria 등등)

MongoTemplate이 사용하는 default Converter는 `MappingMongoConverter`임.

`WriteResultChecking` 값은 기본적으로 NONE이지만 EXCEPTION으로 변경 가능. 변경하면, 몽고 디비 업데이트 실패 시에 예외를 발생시킴.

```java
@Document(collection = "accounts")
public class Account {

    @Id
    private String id;

    private String username;

    private String email;

    public Account(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "Account{" +
               "id='" + id + '\'' +
               ", username='" + username + '\'' +
               ", email='" + email + '\'' +
               '}';
    }
}

```


```java
@Component
public class AppRunner implements ApplicationRunner {

    @Autowired
    MongoTemplate template;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Drop Collection
        template.dropCollection(Account.class);

        // Insert
        Account account1 = new Account("youngjae park", "youngjae.p");
        Account account2 = new Account("youngjae p", "youngjae.p");
        Account account3 = new Account("youngjae", "youngjae.park");

        template.insert(account1);
        template.insert(account2);
        template.insert(account3);

        // Find
        Account findAccount = template.findById(account1.getId(), Account.class);

        // Update
        template.updateFirst(Query.query(Criteria.where("username").is("youngjae park")),
                             Update.update("email", "NONE"), Account.class);

        // Delete
        template.remove(account2);

        // find All
        template.findAll(Account.class).forEach(System.out::println);

    }
}
```

```
[실행결과]
Account{id='5f435e0c78b669750f5ef91a', username='youngjae park', email='NONE'}
Account{id='5f435e0c78b669750f5ef91c', username='youngjae', email='youngjae.park'}
```



Document의 ID는 객체의 @id 애노테이션이 붙은 곳과 매핑됩니다. String, BigInteger, ObjectId 세 가지 사용 가능.


다큐먼트가 아닐 경우 저장할 때 Cascading 가능함. 다큐먼트일 경우 하나씩 insert(or save)를 호출해야 함.
save는 아이디가 이미 존재할 경우, Update하며, 아이디가 없는 경우 Insert함. (Upsert)

- insert(Object obj) : obj를 컬렉션에 저장함.
- insert(Object obj, String collectionName) : obj를 collectionName에 저장함.


```java
    @Test
    void saveCascading() {
        Child child = new Child("child");
        Parent parent = new Parent("parent", child);

        mongoTemplate.save(parent);
    }
```



- updateFirst : 쿼리 문서와 일치하는 첫 번째 문서를 업데이트
- updateMulti : 쿼리 문서와 일치하는 모든 문서 업데이트


- findAndModify : 데이터를 찾은 후에 수정이 가능하다. 해당하는 모든 데이터를 수정함. 리턴 값으로 객체를 반환하지만, 해당 객체는 oldValue임에 주의하자. 필요하다면 다시 조회해와야함.
    - FindAndModifyOptions의 returnNew()를 true로 설정하면 바로 새 데이터 반환하게 할 수 있음.

```java
    @Test
    void findAndModify() {

        mongoTemplate.dropCollection(Parent.class);

        Child child = new Child("child", 3);
        Parent parent = new Parent("parent", 32, child);

        mongoTemplate.save(parent);

        // return OldValue
        Parent oldParent = mongoTemplate.findAndModify(Query.query(Criteria.where("name").is("parent")),
                                                       new Update().inc("age", 1), Parent.class);

        assertEquals(32, oldParent.getAge());
        assertEquals(3, oldParent.getChild().getAge());

        // return newValue
        Parent findParent =  mongoTemplate.findOne(Query.query(Criteria.where("name").is("parent")), Parent.class);

        assertEquals(33, findParent.getAge());
        assertEquals(3, findParent.getChild().getAge());
    }
```

- 이렇게도 할 수 있음.

```java
    @Test
    void findAndModify() {

        mongoTemplate.dropCollection(Parent.class);

        Child child = new Child("child", 3);
        Parent parent = new Parent("parent", 32, child);

        mongoTemplate.save(parent);

        // return OldValue
        Parent oldParent = mongoTemplate.update(Parent.class)
                                .matching(Criteria.where("name", "parent"))
                                .apply(new Update().inc("age",1))
                                .findAndModifyValue();

        // return newValue
        Parent findParent =  mongoTemplate.update(Parent.class)
                                .matching(Criteria.where("name", "parent"))
                                .apply(new Update().inc("age",1))
                                .withOptions(FindAndModifyOptions.options().returnNew(true))
                                .findAndModifyValue();
    }


```


- Query 사용해보기.

```java
    @Test
    void queryDocumentsInACollection() {
        mongoTemplate.dropCollection(Parent.class);

        Child child = new Child("child", 3);
        Parent parent = new Parent("parent", 32, child);

        Child child2 = new Child("child2", 3);
        Parent parent2 = new Parent("parent2", 32, child2);

        Child child3 = new Child("child2", 3);
        Parent parent3 = new Parent("parent2", 32, child3);

        mongoTemplate.save(parent);
        mongoTemplate.save(parent2);
        mongoTemplate.save(parent3);    //same content with parent2

        List<String> result = mongoTemplate.query(Parent.class)
                                           .distinct("name")
                                           .matching(Query.query(Criteria.where("age").gt(30)))
                                           .as(String.class)
                                           .all();

        assertEquals(2, result.size());
        assertEquals("parent", result.get(0));
        assertEquals("parent2", result.get(1));
    }
```

- 데이터가 중복되었을 때, distinct로 걸러낼 수 있지만, 반환타입에도 제약이 생김.


## Working with Spring Data Repositories

```java
public interface ParentRepository extends CrudRepository<Parent, String> {

    List<Parent> findByName(String name);
}
```

```java
    @Test
    void findByName() {
        mongoTemplate.dropCollection(Parent.class);

        Child child = new Child("child", 3);
        Parent parent = new Parent("parent", 32, child);

        Child child2 = new Child("child2", 3);
        Parent parent2 = new Parent("parent2", 32, child2);

        Child child3 = new Child("child2", 3);
        Parent parent3 = new Parent("parent2", 32, child3);

        // save()는 CrudRepository에 이미 존재하기 때문에 Repository Proxy를 바로 사용할 수 있음..
        parentRepository.save(parent);
        parentRepository.save(parent2);
        parentRepository.save(parent3);    //same content with parent2

        // 메서드 이름 규칙에 따라 Proxy에 기능을 구현해줌.
        List<Parent> parents = parentRepository.findByName("parent2");

        assertEquals(2, parents.size());
    }
```

- Child의 이름으로도 찾을 수 있음. findChildName --> x.child.name을 의미함.

```java

    @Test
    void findByChildName() {
        initTestData();

        List<Parent> parents = parentRepository.findByChildName("child2");

        assertEquals(2, parents.size());

    }

    public void initTestData() {
        mongoTemplate.dropCollection(Parent.class);
        mongoTemplate.dropCollection(Child.class);

        Child child = new Child("child", 3);
        Parent parent = new Parent("parent", 32, child);

        Child child2 = new Child("child2", 3);
        Parent parent2 = new Parent("parent2", 32, child2);

        Child child3 = new Child("child2", 3);
        Parent parent3 = new Parent("parent2", 32, child3);

        childRepository.saveAll(Arrays.asList(child, child2, child3));
        parentRepository.saveAll(Arrays.asList(parent, parent2, parent3));
    }

```


- 당연히 Paging도 가능함.


```java
    @Test
    void findPageByName() {
        initTestData();

        Page<Parent> page = parentRepository.findPageByName("parent2", PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getContent().size());
    }
```



