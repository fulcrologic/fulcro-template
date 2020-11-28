test:
	npm install
	npx shadow-cljs compile ci-tests
	npx karma start --single-run
	clj -M:dev:clj-tests

.PHONY: test
