
{:foreign-libs [{:file "bar.js"
                 :provides ["foo.bar"]}
                {:file "mithril.js"
                 :provides ["m"]}
                {:file "crudbox.js"
                 :provides ["crud" "client" "setup_dropbox"]}]

 :externs ["bar.ext.js" "mithril.ext.js" "crudbox.ext.js"]}
