
# Modern Clojurescript adapted

This repository started as working environment for Modern Cljs,
but is now a Clojurescript web development experimentation using
Mithril from within Cljs.

It sort of kind of works.

## Regular dev environment

  boot dev

## Just Clojurescript build

  boot cljs target -d target

## REPL connection

  boot repl -c

## For production use advanced optimization

Though currently warns about how set! updates things in db, since it might not work on older JS...

  boot cljs -O advanced target -d target

Also, for some reason first rendering does not produce anything nor complain about nothing,
but afterwards calling updatedb does yield regular content rendering. Perhaps for some reason
not everything is ready at the point where Mithril is already mounting, since it manages to
produce the DIV it should though devoid of content.
