FROM ruby:2.7.5

RUN apt-get update && apt-get install -y nodejs
WORKDIR /app
COPY Gemfile* .
RUN bundle install
COPY . .
CMD ["rails", "server"]