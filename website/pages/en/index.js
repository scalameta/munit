/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

const React = require("react");

const CompLibrary = require("../../core/CompLibrary.js");
const Container = CompLibrary.Container;
const GridBlock = CompLibrary.GridBlock;

const siteConfig = require(process.cwd() + "/siteConfig.js");

function docUrl(doc, language) {
  return siteConfig.baseUrl + "docs/" + (language ? language + "/" : "") + doc;
}

class Button extends React.Component {
  render() {
    return (
      <div className="pluginWrapper buttonWrapper">
        <a className="button" href={this.props.href} target={this.props.target}>
          {this.props.children}
        </a>
      </div>
    );
  }
}

Button.defaultProps = {
  target: "_self"
};

const SplashContainer = props => (
  <div className="homeContainer">
    <div className="homeSplashFade">
      <div className="wrapper homeWrapper">{props.children}</div>
    </div>
  </div>
);

const ProjectTitle = props => (
  <h2 className="projectTitle">
    {siteConfig.title}
    <small>{siteConfig.tagline}</small>
  </h2>
);

const PromoSection = props => (
  <div className="section promoSection">
    <div className="promoRow">
      <div className="pluginRowBlock">{props.children}</div>
    </div>
  </div>
);

class HomeSplash extends React.Component {
  render() {
    let language = this.props.language || "";
    return (
      <SplashContainer>
        <div className="inner">
          <ProjectTitle />
          <PromoSection>
            <Button href={docUrl("getting-started.html", language)}>
              Get Started
            </Button>
          </PromoSection>
        </div>
      </SplashContainer>
    );
  }
}

const Block = props => (
  <Container
    padding={["bottom", "top"]}
    id={props.id}
    background={props.background}
  >
    <GridBlock align="left" contents={props.children} layout={props.layout} />
  </Container>
);

const Features = props => {
  const features = [
    {
      title: "Source locations for errors",
      content:
        "Test failures point to the source code location where the failure happened. " +
        "Cmd+click on the filename to open the relevant line number in your editor (does not work in all terminals).",
      image: "https://i.imgur.com/goYdJhw.png",
      imageAlign: "left"
    },
    {
      title: "Helpful diffs",
      content:
        "Assertion failures show the difference between the expected and obtained behavior. " +
        "Diffs for case classes include field names in Scala 2.13.",
      image: "https://i.imgur.com/NaAU2He.png",
      imageAlign: "right"
    },
    {
      title: "Highlighted stack traces",
      content:
        "Classes that are defined in your workspace are highlighted in stack traces " +
        "making it easier to quickly understand an error.",
      image: "https://i.imgur.com/iosErEv.png",
      imageAlign: "left"
    },
    {
      title: "IDE support",
      content:
        "Run MUnit test suites directly from the comfort of your IDE, whether it's IntelliJ, VS Code, or any other LSP editor.<br/><br/>" +
        '<div style="display: flex; gap: 20px; align-items: center; justify-content: center; margin-top: 20px;">' +
        '<img src="img/scalameta-logo.png" alt="Metals Logo" style="height: 100px;" />' +
        '<img src="https://resources.jetbrains.com/storage/products/company/brand/logos/IntelliJ_IDEA_icon.svg" alt="IntelliJ IDEA Logo" style="height: 100px;" />' +
        '</div>',
      imageAlign: "right"
    }
  ];
  return (
    <div
      className="productShowcaseSection paddingBottom"
      style={{ textAlign: "left" }}
    >
      {features.map(feature => (
        <Block key={feature.title}>{[feature]}</Block>
      ))}
    </div>
  );
};
class Index extends React.Component {
  render() {
    let language = this.props.language || "";

    return (
      <div>
        <HomeSplash language={language} />
        <Features />
      </div>
    );
  }
}

module.exports = Index;
