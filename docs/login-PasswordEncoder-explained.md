Dit is een **uitstekende vraag**! Het verschil zit hem in **overriden vs implementeren**.

## Het verschil uitgelegd:

### ❌ Waarom mag dit NIET (jouw originele code):

```java
return new BCryptPasswordEncoder() {
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // Deze methode PROBEERT een FINAL methode te overriden
    }
};
```

**Reden:** `matches` is een `final` methode in `BCryptPasswordEncoder`. Je kunt een `final` methode **niet** overriden (overschrijven) in een subclass.

### ✅ Waarom mag dit WEL (nieuwe code):

```java
return new PasswordEncoder() {  // ← Interface, geen class!
    private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();
    
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // Je implementeert de interface, je overridet geen final methode!
    }
};
```

**Reden:** `PasswordEncoder` is een **interface**, geen class. Je implementeert de methode `matches` voor het eerst, je overridet hem niet!

## Visualisatie van het verschil:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    SITUATIE 1: Subclass van BCryptPasswordEncoder           │
│                              (NIET TOEGESTAAN)                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  BCryptPasswordEncoder (class)                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ public final boolean matches(...) { ... }  ← FINAL methode!         │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    ↑                                        │
│                                    │                                        │
│                    new BCryptPasswordEncoder() {                            │
│                        @Override                                            │
│                        public boolean matches(...) {  ← ❌ PROBEERT te      │
│                            // Kan niet! Methode is final!                  │
│                        }                                                    │
│                    }                                                        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                    SITUATIE 2: Implementatie van PasswordEncoder            │
│                              (WEL TOEGESTAAN)                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  PasswordEncoder (interface)                                                │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ boolean matches(CharSequence rawPassword, String encodedPassword);   │   │
│  │ // Dit is een ABSTRACTE methode (geen implementatie!)               │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    ↑                                        │
│                                    │                                        │
│                    new PasswordEncoder() {  ← Implementeert interface       │
│                        @Override                                            │
│                        public boolean matches(...) {  ← ✅ MAG wel!        │
│                            // Eerste implementatie van de methode          │
│                        }                                                    │
│                    }                                                        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Wat is een `final` methode?

```java
public class BCryptPasswordEncoder {
    
    // 'final' betekent: deze methode mag NIET worden overriden
    public final boolean matches(CharSequence rawPassword, String encodedPassword) {
        // Implementatie
    }
}

// ❌ Dit mag niet
public class MyEncoder extends BCryptPasswordEncoder {
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // Compiler error: Cannot override the final method from BCryptPasswordEncoder
    }
}
```

## Wat is een interface?

```java
public interface PasswordEncoder {
    // Dit is een abstracte methode (geen implementatie)
    boolean matches(CharSequence rawPassword, String encodedPassword);
}

// ✅ Dit mag wel
PasswordEncoder myEncoder = new PasswordEncoder() {
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // Je geeft de EERSTE implementatie van deze methode
        return true;
    }
};
```

## Delegate pattern (wat je gebruikt):

```java
return new PasswordEncoder() {
    private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();
    
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // Je roept de FINAL methode aan van delegate (dat mag wel!)
        // Je overridet hem niet, je roept hem alleen aan!
        return delegate.matches(rawPassword, encodedPassword);
    }
};
```

Dit is het **Delegate Pattern**:
- Je implementeert de interface `PasswordEncoder`
- Je DELEGEERT (geeft door) de echte werk naar `BCryptPasswordEncoder`
- Je mag de `final` methode van `BCryptPasswordEncoder` niet overriden, maar je mag hem WEL aanroepen!

## Vergelijking in een tabel:

| Aspect | Subclass (❌) | Delegate/Interface (✅) |
|--------|---------------|------------------------|
| **Wat maak je?** | Nieuwe class die extends | Nieuwe class die implements interface |
| **Methode status** | Probeert FINAL methode te overriden | Geeft EERSTE implementatie van abstracte methode |
| **Compileert?** | Nee, compiler error | Ja, werkt perfect |
| **Roep je de BCrypt methode aan?** | Nee, je probeert hem te vervangen | Ja, je roept hem aan via delegate |

## Analogie:

```
Stel je voor: BCryptPasswordEncoder is een BOEK met een FINALE pagina.

❌ Subclass proberen:
   Je probeert die FINALE pagina te vervangen met een andere pagina.
   → Mag niet, boek zegt: "Deze pagina is FINAL!"

✅ Delegate pattern:
   Je schrijft een NIEUW boek (implementeert PasswordEncoder interface).
   In jouw boek zet je een verwijzing naar het originele boek (delegate).
   Je mag de pagina uit het originele boek LEZEN (aanroepen), maar niet vervangen.
```

## Conclusie:

| Wat | Waarom |
|-----|--------|
| `new BCryptPasswordEncoder() { @Override matches() }` | ❌ `matches` is `final` in `BCryptPasswordEncoder` |
| `new PasswordEncoder() { @Override matches() }` | ✅ Je implementeert een interface, je overridet geen `final` methode |

**De truc:** Je implementeert de interface `PasswordEncoder` (wat altijd mag) en gebruikt een `delegate` (`BCryptPasswordEncoder`) om het echte werk te doen. Je overridet niks, je implementeert alleen! 🎯